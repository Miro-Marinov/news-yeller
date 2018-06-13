package kafka

import akka.stream.ActorMaterializer
import com.google.inject.{Guice, Inject}
import com.lightbend.kafka.scala.streams.DefaultSerdes._
import com.lightbend.kafka.scala.streams.ImplicitConversions._


object ProcessorApp extends App {
  val injector = Guice.createInjector(MainModule)
  injector.getInstance(classOf[ProcessorApp]).run()
}

class ProcessorApp @Inject()(kafkaProcessor: KafkaProcessor)(implicit m: ActorMaterializer) {
  def run() = {
//    kafkaProcessor.aggregated.to("aggregated-tweets")
    kafkaProcessor.aggregated.to("aggregated-tweets")
    kafkaProcessor.run()
  }
}

