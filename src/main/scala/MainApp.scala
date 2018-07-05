package finrax

import java.time.Instant
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.util.Locale

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Route
import akka.pattern.{Backoff, BackoffSupervisor}
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Keep, Sink, Source}
import akka.util.Timeout
import com.google.inject.{Guice, Inject}
import actor.{PrintlnActor, TopNActor, TwitterActor}
import finrax.clients.cryptocontrol.CryptoControlClient
import finrax.clients.reddit.RedditClient
import finrax.clients.rssfeed.RssFeedClient
import finrax.clients.twitter.TwitterClient
import finrax.clients.twitter.domain.entities.Tweet
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
                        printActor: ActorRef)
                       (implicit actorSystem: ActorSystem, materializer: Materializer, ec: ExecutionContext) {

  def run(): Unit = {
     val host = "localhost"
     val port = 8014

     val (actorRef, sseSource) =
       Source.actorRef[String](1000, akka.stream.OverflowStrategy.dropTail)
         .map(s => ServerSentEvent(s))
         .keepAlive(30 seconds, () => ServerSentEvent.heartbeat)
         .toMat(BroadcastHub.sink[ServerSentEvent])(Keep.both)
         .run()

    def startTwitterStream = {
      val twitterTopNActorProps = TopNActor.props[Tweet](actorRef, 20, "twitter")
      val supervisorProps = backOffSupervisorProps("twitterActor", twitterTopNActorProps)
      val twitterActorSupervisor = actorSystem.actorOf(supervisorProps, name = "twitterActorSupervisor")
      twitterClient.getStatusesFilterStream
        .mapAsync(5) { tweet =>
          import akka.pattern.ask
          implicit val askTimeout: Timeout = Timeout(5 seconds)
          twitterActorSupervisor ? tweet
        } runWith Sink.ignore
    }

    def startRedditStreams = {
      val redditTopNActorProps = TopNActor.props[Tweet](actorRef, 20, "reddit")
      val supervisorProps = backOffSupervisorProps("redditActor", redditTopNActorProps)
      val redditActorSupervisor = actorSystem.actorOf(supervisorProps, name = "redditActorSupervisor")


      redditClient.stream("Bitcoin").mapAsync(5) { redditPosts =>
        import akka.pattern.ask
        implicit val askTimeout: Timeout = Timeout(5 seconds)
        redditActorSupervisor ? redditPosts
      } runWith Sink.ignore



    }


    def backOffSupervisorProps(childName: String, childProps: Props) = BackoffSupervisor.props(
      Backoff.onStop(
        childName = childName,
        childProps = childProps,
        minBackoff = 5 seconds,
        maxBackoff = 30 seconds,
        randomFactor = 0.2
      )
    )

/*     twitterClient.getStatusesFilterStream
       .mapAsync(4) { tweet =>
         println(tweet)
         import akka.pattern.ask
         implicit val askTimeout: Timeout = Timeout(30 seconds)
         twitterAggregatingActorSupervisor ? tweet
       } runWith Sink.ignore*/

    twitterClient.getStatusesFilterStream.runWith(Sink foreach println)


     val route: Route = routes.sse("twitter", sseSource)
     val binding: Future[ServerBinding] = Http().bindAndHandle(route, host, port)
     binding.onComplete(_ => println(s"Server running bound to: $host:$port"))


    //    RssFeed("https://cointelegraph.com/rss").stream()
    //    https://www.reddit.com/r/CryptoCurrency.rss
    //        rssFeed.stream("https://www.reddit.com/r/CryptoCurrency.rss").runWith(Sink foreach println)
    /*    val p = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").parse("Thu, 14 Jun 2018 18:23:16 +0000")
        p*/
    //    client.reddit.stream("Bitcoin") map(_.sorted.take(10)) runWith(Sink foreach println)

//    cryptoControlClient.pollNewsAndSendToAggregator
  }
}
