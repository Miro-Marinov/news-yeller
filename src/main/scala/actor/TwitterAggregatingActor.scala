package actor


import java.time.Instant
import java.time.temporal.ChronoUnit

import actor.TwitterAggregatingActor.{Ack, State}
import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.{PersistentActor, SnapshotOffer, SnapshotSelectionCriteria}
import finrax.BoundedPriorityQueue
import org.json4s.native.Serialization
import serializaiton.JsonSupport
import twitter.domain.entities.Tweet


class TwitterAggregatingActor(sourceActor: ActorRef, n: Int, topic: String) extends PersistentActor with ActorLogging with JsonSupport {
  val snapShotMsgInterval = 100
  var state = State(new BoundedPriorityQueue[Tweet](n))


  def updateState(tweet: Tweet): Boolean = {
    state.update(tweet)
  }

  val receiveRecover: Receive = {
    case SnapshotOffer(_, snapshot: State) => state = snapshot
    case tweet: Tweet => updateState(tweet)
  }

  def filterTweet(tweet: Tweet): Boolean = {
    tweet.retweeted && tweet.retweet_count > 30
  }

  val receiveCommand: Receive = {
    case tweet: Tweet =>
      processTweet(tweet)
      sender ! Ack
    case tweets: Seq[Tweet] =>
      tweets.foreach(processTweet)
      sender ! Ack
    case "print" ⇒ println(state)
  }

  def processTweet(tweet: Tweet): Unit = {
    Some(tweet).filter(tweet => filterTweet(tweet)) .foreach { tweet =>
      persist(tweet) { event ⇒
        if (updateState(event)) {
          val jsonState = Serialization.write(state.topNTweets.toList)
          sourceActor ! Serialization.write(jsonState)
        }
        context.system.eventStream.publish(event)
        if (lastSequenceNr % snapShotMsgInterval == 0 && lastSequenceNr != 0) {
          saveSnapshot(state)
          deleteMessages(lastSequenceNr)
          val nowMinusTwoWeeks = Instant.now().minus(14, ChronoUnit.DAYS)
          deleteSnapshots(SnapshotSelectionCriteria.apply(maxTimestamp = nowMinusTwoWeeks.getEpochSecond))
        }
      }
    }
  }

  override def persistenceId: String = topic
}

object TwitterAggregatingActor {

  def props(sourceActor: ActorRef, n: Int, topic: String): Props = Props[TwitterAggregatingActor](new TwitterAggregatingActor(sourceActor, n, topic))

  case object Init

  case object Ack

  case object Complete

  case class State(topNTweets: BoundedPriorityQueue[Tweet]) {
    def update(tweet: Tweet): Boolean = topNTweets add tweet

    def size: Int = topNTweets.size()

    override def toString: String = topNTweets.toString
  }

}


