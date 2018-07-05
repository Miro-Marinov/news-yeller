package finrax.util

import akka.actor.Scheduler
import akka.pattern.after
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}


object FutureUtil extends LazyLogging {
  def retry[T](minDelay: FiniteDuration, maxDelay: FiniteDuration, scalingFactor: Double)(f: => Future[T])(implicit ec: ExecutionContext, s: Scheduler): Future[T] = {
    def retryInner(curDelay: FiniteDuration): Future[T] = {
      val nextDelay = Some(curDelay.mul(scalingFactor))
        .collect { case x: FiniteDuration => x }
        .fold(maxDelay)(_ min maxDelay)

      f recoverWith { case exc =>
        logger.error(s"Retrying after $curDelay", exc)
        after(curDelay, s)(retryInner(nextDelay))
      }
    }

    retryInner(minDelay)
  }
}
