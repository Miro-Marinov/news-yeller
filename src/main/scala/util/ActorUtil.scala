package finrax.util

import akka.actor.Props
import akka.pattern.{Backoff, BackoffSupervisor}
import scala.concurrent.duration._

object ActorUtil {
  def backOffSupervisorProps(childName: String, childProps: Props): Props = BackoffSupervisor.props(
    Backoff.onStop(
      childName = childName,
      childProps = childProps,
      minBackoff = 5 seconds,
      maxBackoff = 30 seconds,
      randomFactor = 0.2
    )
  )
}
