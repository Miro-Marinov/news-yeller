package finrax

import actor.aggregator.{AggregatingActor, AggregatingActorConfig}
import akka.actor.{ActorRef, ActorSystem}
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
import finrax.http.NewsController

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}


object MainApp extends App {
  val injector = Guice.createInjector(MainModule)
  injector.getInstance(classOf[MainApp]).run()
}

class MainApp @Inject()(newsController: NewsController,
                        rssFeedClient: RssFeedClient,
                        twitterClient: TwitterClient,
                        redditClient: RedditClient,
                        cryptoControlClient: CryptoControlClient,
                        aggregatingActorConfig: AggregatingActorConfig)
                       (implicit actorSystem: ActorSystem, m: Materializer, ec: ExecutionContext) extends LazyLogging {

  def run(): Unit = {

    // Actor serving as a source - used to push server sent events to the clients
    val (sseActorRef, sseSource) =
      // Buffers the last 50 server sent events - drops the last when overflowed
      Source.actorRef[String](50, akka.stream.OverflowStrategy.dropTail)
        .map(s => ServerSentEvent(s))
        // Send a heartbeat every 30 seconds to keep the connection alive
        .keepAlive(30 seconds, () => ServerSentEvent.heartbeat)
        .toMat(BroadcastHub.sink[ServerSentEvent])(Keep.both)
        .run()

    val aggregator: ActorRef = actorSystem.actorOf(AggregatingActor.props(sseActorRef,
      "news-aggregator",
      aggregatingActorConfig))

    twitterClient.pollAndSendToAggregator(aggregator)
    redditClient.pollAndSendToAggregator(aggregator)
    rssFeedClient.pollAndSendToAggregator(aggregator)
    cryptoControlClient.pollAndSendToAggregator(aggregator)

    val host = "localhost"
    val port = 8014

    val route: Route = newsController.api(sseSource, aggregator)
    val binding: Future[ServerBinding] = Http().bindAndHandle(route, host, port)
    binding.onComplete(_ => logger.info(s"Server running bound to: $host:$port"))
  }
}
