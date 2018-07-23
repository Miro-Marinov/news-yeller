package finrax.actor.topn

import java.time.Instant
import java.time.temporal.ChronoUnit

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer, SnapshotSelectionCriteria}
import finrax.actor.topn.TopNActor.Ack

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

class TopNActor[T <: AnyRef : ClassTag : TypeTag, U]
(aggregator: ActorRef, override val persistenceId: String, topNActorConfig: TopNActorConfig)(uniquenessF: T => U)
(implicit ord: Ordering[T])
  extends PersistentActor with ActorLogging {

  val snapShotMsgInterval: Int = topNActorConfig.snapshotIntervalMsgs
  val capacity: Int = topNActorConfig.capacity
  var topN = Vector.empty[T]

  val receiveRecover: Receive = {
    case SnapshotOffer(_, snapshot: Vector[T]) => topN = snapshot
    case event: T => add(event)
    case RecoveryCompleted if topN.nonEmpty =>
      sendStateToAggregator()
  }

  def add(element: T): Boolean = {
    if (topN.size < capacity) {
      topN = (topN :+ element).groupBy(uniquenessF).mapValues(_.head).values.toVector.sorted(ord.reverse)
      true
    }
    else if (ord.gt(element, topN(topN.size - 1))) {
      topN = topN.updated(topN.size - 1, element).groupBy(uniquenessF).mapValues(_.head).values.toVector.sorted(ord.reverse)
      true
    }
    else {
      false
    }
  }

  def sendStateToAggregator(): Unit = {
    // We can't share a mutable collection between actors so
    // we create a new sifted (for unique elements), sorted list to send to the aggregator
    // TODO: We are losing the benefits of the PQ when doing toList.sort
    aggregator ! State(topN)
  }

  val receiveCommand: Receive = {
    case event: T => // TODO: Why do we even need a ClassTag for this to work? And why it doesn't work with TypeTag?
      processEvent(event)
      sender ! Ack
    case events: Seq[T] => //TODO: That is unsafe
      events.foreach(processEvent)
      sender ! Ack
  }

  def processEvent(event: T): Unit = {
    // TODO: Add custom serialization: https://doc.akka.io/docs/akka/2.5/persistence.html#custom-serialization
    // Since persist is async we are modifying the queue outside
    persist(event) { event =>
      if (add(event))
        sendStateToAggregator()

      //TODO: See if this is working
      //TODO: Why do we need T <: AnyRef for this to work????
      context.system.eventStream.publish(event)
      if (lastSequenceNr % snapShotMsgInterval == 0 && lastSequenceNr != 0) {
        saveSnapshot(topN)
        deleteMessages(lastSequenceNr)
        val nowMinusTwoWeeks = Instant.now().minus(7, ChronoUnit.DAYS)
        deleteSnapshots(SnapshotSelectionCriteria.apply(maxTimestamp = nowMinusTwoWeeks.getEpochSecond))
      }
    }
  }
}

object TopNActor {

  def props[T <: AnyRef : Ordering : ClassTag : TypeTag, U](aggregator: ActorRef,
                                                            persistenceId: String,
                                                            topNActorConfig: TopNActorConfig)(uniquenessF: T => U): Props = Props(new TopNActor[T, U](aggregator, persistenceId, topNActorConfig)(uniquenessF))

  case object Init

  case object Ack

  case object Complete

}