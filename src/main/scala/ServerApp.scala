import actor.TwitterAggregatingActor
import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Route
import akka.pattern.{Backoff, BackoffSupervisor}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import akka.util.Timeout
import com.google.inject.{Guice, Inject}
import http.Routes
import kafka.KafkaConsumer
import twitter.domain.entities.Tweet
import twitter.{StatusFilter, TwitterClient}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

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
                          twitterClient: TwitterClient,
                          ec: ExecutionContext) {

  def run() = {
    val host = "localhost"
    val port = 8014

    val trackedWords = Seq("#BTC", "#bitcoin", "#btc", "bitcoin", "btc")

    val filter = StatusFilter(tracks = trackedWords)
    val (actorRef, publisher) = Source.actorRef[Tweet](1000, OverflowStrategy.fail).toMat(Sink.asPublisher(false))(Keep.both).run()
    val source: Source[ServerSentEvent, NotUsed] = Source.fromPublisher(publisher).map(tweet => ServerSentEvent(tweet.toString))

    val twitterAggregatingActorProps = TwitterAggregatingActor.props(actorRef, 15, "crypto")
    val btcBackOffSupervisorProps = BackoffSupervisor.props(
      Backoff.onStop(
        childName = "twitterAggregatingActor",
        childProps = twitterAggregatingActorProps,
        minBackoff = 5 seconds,
        maxBackoff = 30 seconds,
        randomFactor = 0.2
      )
    )

    val route: Route = routes.sse("twitter", source)

    val twitterAggregatingActorSupervisor =
      actorSystem.actorOf(btcBackOffSupervisorProps, name = "twitterAggregatingActorSupervisor")

//    twitterClient.getStatusesFilterStream(filter).runWith(Sink foreach println)
    twitterClient.getStatusesFilterStream(filter)
      .mapAsync(4) { tweet =>
        import akka.pattern.ask
        implicit val askTimeout: Timeout = Timeout(30 seconds)
        twitterAggregatingActorSupervisor ? tweet
      } runWith Sink.ignore

    val binding: Future[ServerBinding] = Http().bindAndHandle(route, host, port)
    binding.onComplete(_ => println(s"Server running bound to: $host:$port"))
  }
}
