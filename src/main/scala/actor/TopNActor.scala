package finrax.actor

import java.time.Instant
import java.time.temporal.ChronoUnit

import finrax.actor.TopNActor.{Ack, State}
import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer, SnapshotSelectionCriteria}
import util.BoundedPriorityQueue

import scala.reflect.ClassTag

class TopNActor[T](aggregator: ActorRef, n: Int, override val persistenceId: String)
                  (implicit cta: ClassTag[T], ord: Ordering[T]) extends PersistentActor with ActorLogging {
  val snapShotMsgInterval = 100
  var state = State(new BoundedPriorityQueue[T](n))

  def updateState(event: T): Boolean = {
    state.update(event)
  }

  val receiveRecover: Receive = {
    case SnapshotOffer(_, snapshot: State[T]) => state = snapshot
    case event: T => updateState(event)
    case RecoveryCompleted => aggregator ! state
  }

  val receiveCommand: Receive = {
    case event: T =>
      processEvent(event)
      sender ! Ack
    case tweets: Seq[T] =>
      tweets.foreach(processEvent)
      sender ! Ack
    case "print" ⇒ println(state)
  }

  def processEvent(event: T): Unit = {
    persist(event) { event =>
      if (updateState(event)) {
        aggregator ! state
      }
      //TODO: See if this is working
      context.system.eventStream.publish(event.getClass)
      if (lastSequenceNr % snapShotMsgInterval == 0 && lastSequenceNr != 0) {
        saveSnapshot(state)
        deleteMessages(lastSequenceNr)
        val nowMinusTwoWeeks = Instant.now().minus(14, ChronoUnit.DAYS)
        deleteSnapshots(SnapshotSelectionCriteria.apply(maxTimestamp = nowMinusTwoWeeks.getEpochSecond))
      }
    }
  }
}

object TopNActor {

  def props[T](sourceActor: ActorRef, n: Int, persistenceId: String)
              (implicit cta: ClassTag[T], ord: Ordering[T]): Props = Props(new TopNActor[T](sourceActor, n, persistenceId))

  case object Init

  case object Ack

  case object Complete

  case class State[T](topN: BoundedPriorityQueue[T]) {
    def update(event: T): Boolean = topN add event

    def size: Int = topN.size()

    override def toString: String = topN.toString
  }
}