import akka.Done
import com.google.inject.{Guice, Inject}
import kafka.KafkaConsumer
import twitter.domain.entities.Tweet

import scala.concurrent.Future

object ConsumerApp extends App {
  val injector = Guice.createInjector(MainModule)
  injector.getInstance(classOf[ConsumerApp]).run()
}

class ConsumerApp @Inject()(kafkaConsumer: KafkaConsumer) {
  def run(): Future[Done] = {
    kafkaConsumer.consume[Tweet]("aggregated-tweets") { tweets =>
//      println(tweets)
      {}
    }
  }
}

