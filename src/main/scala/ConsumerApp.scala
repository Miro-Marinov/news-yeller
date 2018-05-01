import com.google.inject.{Guice, Inject}
import kafka.KafkaConsumer
import twitter.domain.entities.Tweet

object ConsumerApp extends App {
  val injector = Guice.createInjector(MainModule)
  injector.getInstance(classOf[ConsumerApp]).run()
}

class ConsumerApp @Inject()(kafkaConsumer: KafkaConsumer) {
  def run() = {
    kafkaConsumer.consume[Tweet]("twitter") { tweet =>
      println(tweet.text)
    }
  }
}

