package kafka.config

import java.lang

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, ProducerSettings}
import com.google.inject.{Inject, Singleton}
import org.apache.kafka.common.serialization._

@Singleton
case class KafkaConfig @Inject()(implicit system: ActorSystem) {

  val bootstrapServers: String = system.settings.config.getString("akka.kafka.bootstrap-servers")
  // See application.conf: akka.kafka.consumer
  val consumerSettings: ConsumerSettings[lang.Long, String] =
    ConsumerSettings(system, new LongDeserializer, new StringDeserializer)
    .withBootstrapServers(bootstrapServers)
    .withGroupId("grp-1")
    .withClientId("client-1")

  val producerSettings: ProducerSettings[lang.Long, String] =
    ProducerSettings(system, new LongSerializer, new StringSerializer)
    .withBootstrapServers(bootstrapServers)


}