package actor


import java.time.Instant
import java.time.temporal.ChronoUnit

import actor.TwitterAggregatingActor.{Ack, State}
import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.{PersistentActor, SnapshotOffer, SnapshotSelectionCriteria}
import twitter.domain.entities.Tweet


class TwitterAggregatingActor(sourceActor: ActorRef, n: Int, topic: String) extends PersistentActor with ActorLogging {
  val snapShotMsgInterval = 1000
  var state = State(new BoundedPriorityQueue[Tweet](n))


  def updateState(tweet: Tweet): Boolean = {
    state.update(tweet)
  }

  val receiveRecover: Receive = {
    case SnapshotOffer(_, snapshot: State) => state = snapshot
    case tweet: Tweet => updateState(tweet)
  }

  def filterTweet(tweet: Tweet) = true

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
    println("HERE2")

    Some(tweet) /*.filter(tweet => filterTweet(tweet))*/ .foreach { tweet =>
      persist(tweet) { event ⇒
        println("HERE")
        if (updateState(event)) {
          println("HERE")
          sourceActor ! state
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


