package kafka.config

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, ProducerSettings}
import com.google.inject.{Inject, Singleton}
import org.apache.kafka.common.serialization._
import serializaiton.ScalaLongSerializer

@Singleton
case class KafkaConfig @Inject()(scalaLongSerializer: ScalaLongSerializer,
                                 stringSerializer: StringSerializer,
                                 stringDeserializer: StringDeserializer)
                                (implicit system: ActorSystem) {

  val bootstrapServers: String = system.settings.config.getString("akka.kafka.bootstrap-servers")

  // See application.conf: akka.kafka.consumer
  val consumerSettings: ConsumerSettings[String, String] =
    ConsumerSettings(system, stringDeserializer, stringDeserializer)
      .withBootstrapServers(bootstrapServers)
      .withGroupId("consumer-grp")
      .withClientId("news-yeller")

  val producerSettings: ProducerSettings[String, String] =
    ProducerSettings(system, stringSerializer, stringSerializer)
      .withBootstrapServers(bootstrapServers)
}