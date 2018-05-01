import akka.stream.ActorMaterializer
import com.google.inject.{Guice, Inject}
import kafka.KafkaProducer
import twitter.{StatusFilter, TwitterClient}


object ProducerApp extends App {
  val injector = Guice.createInjector(MainModule)
  injector.getInstance(classOf[ProducerApp]).run()
}

class ProducerApp @Inject()(twitterClient: TwitterClient,
                            kafkaProducer: KafkaProducer)(implicit m: ActorMaterializer) {
  def run() = {
    val trackedWords = Seq("#crypto", "#bitcoin", "#cryptocurrency")
    val filter = StatusFilter(tracks = trackedWords)
    //    twitterClient.getStatusesFilterStream(filter).runWith(Sink.foreach(tweet => println(tweet.text)))
    twitterClient.getStatusesFilterStream(filter).runWith(kafkaProducer.twitter)
  }
}

