package finrax.actor.aggregator

import java.time.Instant
import java.time.temporal.ChronoUnit

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer, SnapshotSelectionCriteria}
import client.reddit.domain.RedditPost
import finrax.client.cryptocontrol.domain.Article
import finrax.clients.rssfeed.RssFeedEntry
import finrax.clients.twitter.domain.entities.Tweet
import finrax.serializaiton.JsonSerialization.finraxFormats
import org.json4s.native.Serialization

class AggregatingActor(sseSourceActor: ActorRef, override val persistenceId: String, aggregatingActorConfig: AggregatingActorConfig) extends PersistentActor with ActorLogging {
  var state = State()
  val snapShotMsgInterval: Int = aggregatingActorConfig.snapshotIntervalMsgs

  val updateFunction: PartialFunction[Any, Unit] = {
    //TODO: How does this GenericMessage.conformsTo work???
    case redditPosts: finrax.actor.topn.State[RedditPost@unchecked] if redditPosts.conformsTo[finrax.actor.topn.State[RedditPost]] =>
      state = state.copy(redditPosts = redditPosts.values)
    case rssFeedNews: finrax.actor.topn.State[RssFeedEntry@unchecked] if rssFeedNews.conformsTo[finrax.actor.topn.State[RssFeedEntry]] =>
      state = state.copy(rssFeedNews = rssFeedNews.values)
    case cryptocontrolNews: finrax.actor.topn.State[Article@unchecked] if cryptocontrolNews.conformsTo[finrax.actor.topn.State[Article]] =>
      state = state.copy(cryptocontrolNews = cryptocontrolNews.values)
    case tweets: finrax.actor.topn.State[Tweet@unchecked] if tweets.conformsTo[finrax.actor.topn.State[Tweet]] =>
      state = state.copy(tweets = tweets.values)
  }

  val handleEventFunction: PartialFunction[Any, Unit] = {
    case topNState: finrax.actor.topn.State[_] => persist(topNState) { persisted =>
      updateFunction(persisted)
      // TODO: Make it so the state is sent to the source at intervals of, say, 30 seconds using akka scheduler making the actor sending a message to itself
      sendToSseSourceActor()

      if (lastSequenceNr % snapShotMsgInterval == 0 && lastSequenceNr != 0) {
        val defCopy = State(state.tweets, state.redditPosts, state.rssFeedNews, state.cryptocontrolNews)
        saveSnapshot(defCopy)
        deleteMessages(lastSequenceNr)
        val nowMinusTwoWeeks = Instant.now().minus(7, ChronoUnit.DAYS)
        deleteSnapshots(SnapshotSelectionCriteria.apply(maxTimestamp = nowMinusTwoWeeks.getEpochSecond))
      }
    }
  }

  def sendToSseSourceActor(): Unit = {
    val defCopy = State(state.tweets, state.redditPosts, state.rssFeedNews, state.cryptocontrolNews)
    sseSourceActor ! Serialization.write(defCopy)
  }

  val receiveRecover: Receive = updateFunction orElse {
    case SnapshotOffer(_, snapshot: State) => state = snapshot
    case RecoveryCompleted => sendToSseSourceActor()
  }

  val receiveCommand: Receive = handleEventFunction
}

object AggregatingActor {
  def props[T](sseSourceActor: ActorRef, persistentId: String, aggregatingActorConfig: AggregatingActorConfig): Props = Props(new AggregatingActor(sseSourceActor, persistentId, aggregatingActorConfig))
}