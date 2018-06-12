package kafka

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerMessage, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import javax.inject.{Inject, Singleton}
import kafka.config.KafkaConfig
import org.json4s.native.Serialization
import serializaiton.JsonSupport

import scala.concurrent.ExecutionContext

@Singleton
class KafkaConsumer @Inject()(kafkaConfig: KafkaConfig)
                             (implicit system: ActorSystem,
                              ec: ExecutionContext,
                              materializer: ActorMaterializer) extends App with JsonSupport {


  def consume[T: Manifest](topic: String)(f: T => Unit) =
    source(topic)
      .runWith(Sink.foreach {
        case (offset, msg) =>
          println(msg)
          val deserialized = Serialization.read[T](msg)
          f(deserialized)
          offset.commitScaladsl()
      })

  def source(topic: String): Source[(ConsumerMessage.CommittableOffset, String), Consumer.Control] =
    Consumer.committableSource(kafkaConfig.consumerSettings, Subscriptions.topics(topic))
      .map(msg => (msg.committableOffset, msg.record.value()))

}
