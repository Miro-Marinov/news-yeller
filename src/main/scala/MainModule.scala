import akka.actor.{ActorSystem, Scheduler}
import akka.stream.{ActorMaterializer, Materializer}
import com.google.inject.AbstractModule
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object MainModule extends AbstractModule {

  def configure(): Unit = {
    implicit val config: Config = ConfigFactory.load()
    implicit val as: ActorSystem = ActorSystem("main-actor-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = as.dispatcher

    bind(classOf[Config]).toInstance(config)
    bind(classOf[ActorSystem]).toInstance(as)
    bind(classOf[Scheduler]).toInstance(as.scheduler)
    bind(classOf[ActorMaterializer]).toInstance(materializer)
    bind(classOf[Materializer]).toInstance(materializer)
    bind(classOf[ExecutionContext]).toInstance(as.dispatcher)
  }
}