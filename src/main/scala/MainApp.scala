package finrax

import actor.TwitterAggregatingActor
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Route
import akka.pattern.{Backoff, BackoffSupervisor}
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Keep, Sink, Source}
import akka.util.Timeout
import com.google.inject.{Guice, Inject}
import di.MainModule
import http.Routes
import kafka.KafkaConsumer
import twitter.{StatusFilter, TwitterClient}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mirob on 8/19/2017.
  */

object MainApp extends App {
  val injector = Guice.createInjector(MainModule)
  injector.getInstance(classOf[MainApp]).run()
}

class MainApp @Inject()(routes: Routes)
                       (implicit kafkaConsumer: KafkaConsumer,
                          actorSystem: ActorSystem,
                          materializer: Materializer,
                          twitterClient: TwitterClient,
                          ec: ExecutionContext) {

  def run() = {
    val host = "localhost"
    val port = 8014

    val trackedWords = Seq("#BTC", "#bitcoin", "#btc", "bitcoin", "btc")

    val filter = StatusFilter(tracks = trackedWords)

    val (actorRef, sseSource) =
      Source.actorRef[String](5, akka.stream.OverflowStrategy.dropTail)
        .map(s => ServerSentEvent(s))
        .keepAlive(5.second, () => ServerSentEvent.heartbeat)
        .toMat(BroadcastHub.sink[ServerSentEvent])(Keep.both)
        .run()

    val twitterAggregatingActorProps = TwitterAggregatingActor.props(actorRef, 15, "twitter")
    val btcBackOffSupervisorProps = BackoffSupervisor.props(
      Backoff.onStop(
        childName = "twitterAggregatingActor",
        childProps = twitterAggregatingActorProps,
        minBackoff = 5 seconds,
        maxBackoff = 30 seconds,
        randomFactor = 0.2
      )
    )


    val twitterAggregatingActorSupervisor =
      actorSystem.actorOf(btcBackOffSupervisorProps, name = "twitterAggregatingActorSupervisor")

    twitterClient.getStatusesFilterStream(filter)
      .mapAsync(4) { tweet =>
        import akka.pattern.ask
        implicit val askTimeout: Timeout = Timeout(30 seconds)
        twitterAggregatingActorSupervisor ? tweet
      } runWith Sink.ignore


    val route: Route = routes.sse("twitter", sseSource)
    val binding: Future[ServerBinding] = Http().bindAndHandle(route, host, port)
    binding.onComplete(_ => println(s"Server running bound to: $host:$port"))
  }
}
