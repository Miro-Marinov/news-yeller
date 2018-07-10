package finrax.di

import akka.actor.{ActorRef, ActorSystem, Scheduler}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Materializer, Supervision}
import com.google.inject.AbstractModule
import com.typesafe.config.{Config, ConfigFactory}
import io.cryptocontrol.cryptonewsapi.CryptoControlApi

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object MainModule extends AbstractModule {

  def configure(): Unit = {
    // Restart each graph stage on failure
    val decider: Supervision.Decider = {
      _ => Supervision.Restart
    }
    val config: Config = ConfigFactory.load()
    val cryptoControlApi: CryptoControlApi = new CryptoControlApi(config.getString("cryptocontrol.apiKey"))
    implicit val actorSystem: ActorSystem = ActorSystem("main-actor-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(actorSystem).withSupervisionStrategy(decider))
    implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

    bind(classOf[Config]).toInstance(config)
    bind(classOf[ActorSystem]).toInstance(actorSystem)
    bind(classOf[Scheduler]).toInstance(actorSystem.scheduler)
    bind(classOf[ActorMaterializer]).toInstance(materializer)
    bind(classOf[Materializer]).toInstance(materializer)
    bind(classOf[ExecutionContext]).toInstance(actorSystem.dispatcher)

    bind(classOf[CryptoControlApi]).toInstance(cryptoControlApi)
  }
}