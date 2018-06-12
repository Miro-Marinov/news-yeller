import actor.TwitterAggregatingActor
import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.pattern.{Backoff, BackoffSupervisor}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.Timeout
import com.google.inject.{Guice, Inject}
import kafka.KafkaProducer
import twitter.domain.entities.Tweet
import twitter.{StatusFilter, TwitterClient}

import scala.concurrent.duration._

object ProducerApp extends App {
  val injector = Guice.createInjector(MainModule)
  injector.getInstance(classOf[ProducerApp]).run()
}

class ProducerApp @Inject()(twitterClient: TwitterClient,
                            kafkaProducer: KafkaProducer)(implicit m: ActorMaterializer, context: ActorSystem) {
  def run(): Unit = {
    val trackedWords = Seq("#BTC", "#bitcoin", "#btc", "bitcoin", "btc")
    val (actorRef, publisher) = Source.actorRef[Tweet](1000, OverflowStrategy.fail).toMat(Sink.asPublisher(false))(Keep.both).run()
    val source: Source[ServerSentEvent, NotUsed] = Source.fromPublisher(publisher).map(tweet => ServerSentEvent(tweet.toString))
    val filter = StatusFilter(tracks = trackedWords)
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

    val twitterAggregatingActorSupervisor =
      context.actorOf(btcBackOffSupervisorProps, name = "twitterAggregatingActorSupervisor")

    twitterClient.getStatusesFilterStream(filter)
      .mapAsync(4) { tweet =>
        import akka.pattern.ask
        implicit val askTimeout: Timeout = Timeout(30 seconds)
        twitterAggregatingActorSupervisor ? tweet
      }
    //    twitterClient.getStatusesFilterStream(filter).runWith(kafkaProducer.twitter(_.id.toString))
  }
}

