import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.google.inject.{Guice, Inject}
import http.Routes
import kafka.KafkaConsumer

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mirob on 8/19/2017.
  */

object ServerApp extends App {
  val injector = Guice.createInjector(MainModule)
  injector.getInstance(classOf[ServerApp]).run()
}

class ServerApp @Inject()(routes: Routes)
                         (implicit kafkaConsumer: KafkaConsumer,
                          actorSystem: ActorSystem,
                          materializer: Materializer,
                          ec: ExecutionContext) {

  def run() = {
    val host = "localhost"
    val port = 8014
    val route: Route = routes.sse("twitter")

    val binding: Future[ServerBinding] = Http().bindAndHandle(route, host, port)
    binding.onComplete(_ => println(s"Server running bound to: $host:$port"))
  }
}
