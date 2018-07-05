package finrax.clients.cryptocontrol

import akka.actor.ActorRef
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import io.cryptocontrol.cryptonewsapi.CryptoControlApi
import io.cryptocontrol.cryptonewsapi.models.Article

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class CryptoControlClient @Inject()(api: CryptoControlApi, aggregatingActor: ActorRef)(implicit m: ActorMaterializer) extends LazyLogging {

  def pollNewsAndSendToAggregator: Future[Done] = {
    Source.tick(1 second, 5 seconds, NotUsed)
      .runForeach { _ =>

        api.getTopNews(new CryptoControlApi.OnResponseHandler[java.util.List[Article]]() {
          override def onSuccess(body: java.util.List[Article]): Unit = {
            aggregatingActor ! body.asScala.sortBy(a => a.getActivityHotness.doubleValue())(Ordering[Double].reverse).toList
          }

          override def onFailure(e: Exception): Unit = {
            logger.error("Error when hitting the client.cryptocontrol api", e)
          }
        })
      }
  }
}
