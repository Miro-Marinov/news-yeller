package finrax.clients.cryptocontrol

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}
import client.cryptocontrol.config.CryptoControlConfig
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import finrax.actor.topn.{TopNActor, TopNActorConfig}
import finrax.client.cryptocontrol.domain.Article
import finrax.util.ActorUtil
import io.cryptocontrol.cryptonewsapi.CryptoControlApi
import io.cryptocontrol.cryptonewsapi.models.{Article => CArticle}

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class CryptoControlClient @Inject()(api: CryptoControlApi, cryptoControlConfig: CryptoControlConfig, topNActorConfig: TopNActorConfig)(implicit actorSystem: ActorSystem, m: ActorMaterializer) extends LazyLogging {

  def pollNewsAndSendToAggregator(aggregator: ActorRef): Future[Done] = {
    val cryptoControlNActorProps = TopNActor.props[Article, String](aggregator, "cryptoControl", topNActorConfig)(article => article.id)
    val supervisorProps = ActorUtil.backOffSupervisorProps("cryptoControlActor", cryptoControlNActorProps)
    val cryptoControlActorSupervisor = actorSystem.actorOf(supervisorProps, name = "cryptoControlActorSupervisor")

    Source.tick(1 second, cryptoControlConfig.pollingIntervalMs millis, NotUsed)
      .runForeach { _ =>
        api.getTopNews(new CryptoControlApi.OnResponseHandler[java.util.List[CArticle]]() {
          override def onSuccess(body: java.util.List[CArticle]): Unit = {
            val articles = body.asScala.map { a => a: Article }.toList
            cryptoControlActorSupervisor ! articles
          }

          override def onFailure(e: Exception): Unit = {
            logger.error("Error when hitting the client.cryptocontrol api", e)
          }
        })
      }
  }
}