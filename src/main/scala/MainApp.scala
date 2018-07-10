package finrax

import actor.aggregator.{AggregatingActor, AggregatingActorConfig}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import com.google.inject.{Guice, Inject}
import com.typesafe.scalalogging.LazyLogging
import finrax.clients.cryptocontrol.CryptoControlClient
import finrax.clients.reddit.RedditClient
import finrax.clients.rssfeed.RssFeedClient
import finrax.clients.twitter.TwitterClient
import finrax.di.MainModule
import finrax.http.Routes

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}


object MainApp extends App {
  val injector = Guice.createInjector(MainModule)
  injector.getInstance(classOf[MainApp]).run()
}

class MainApp @Inject()(routes: Routes,
                        rssFeedClient: RssFeedClient,
                        twitterClient: TwitterClient,
                        redditClient: RedditClient,
                        cryptoControlClient: CryptoControlClient,
                        aggregatingActorConfig: AggregatingActorConfig)
                       (implicit actorSystem: ActorSystem, materializer: Materializer, ec: ExecutionContext) extends LazyLogging {

  def run(): Unit = {
    // Actor serving as a source - used to push server site events to the clients
    val (sseActorRef, sseSource) =
      Source.actorRef[String](1000, akka.stream.OverflowStrategy.dropTail)
        .map(s => ServerSentEvent(s))
        // Send a heartbeat every 30 seconds to keep the connection alive
        .keepAlive(30 seconds, () => ServerSentEvent.heartbeat)
        .toMat(BroadcastHub.sink[ServerSentEvent])(Keep.both)
        .run()

    val aggregator = actorSystem.actorOf(AggregatingActor.props(sseActorRef, "news-aggregator", aggregatingActorConfig))

    twitterClient.startTwitterStream(aggregator)
    redditClient.startRedditStreams(aggregator)
    rssFeedClient.startRssFeedStreams(aggregator)
    cryptoControlClient.pollNewsAndSendToAggregator(aggregator)

    val host = "localhost"
    val port = 8014

    val route: Route = routes.sse("news", sseSource)
    val binding: Future[ServerBinding] = Http().bindAndHandle(route, host, port)
    binding.onComplete(_ => logger.info(s"Server running bound to: $host:$port"))
  }
}
