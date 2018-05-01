package kafka

import java.util.Properties

import com.google.inject.Singleton
import javax.inject.Inject
import kafka.config.KafkaConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.{StreamsBuilder, StreamsConfig}


@Singleton
class KafkaProcessor @Inject()(kafkaConfig: KafkaConfig) {
  val bootstrapServers: String = kafkaConfig.bootstrapServers
  val builder = new StreamsBuilder

  val streamingConfig = {
    val settings = new Properties
    settings.put(StreamsConfig.APPLICATION_ID_CONFIG, "map-function-scala-example")
    settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    // Specify default (de)serializers for record keys and for record values.
    settings.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.ByteArray.getClass.getName)
    settings.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
    settings
  }
  //  import com.lightbend.kafka.scala.streams.DefaultSerdes._

  // Read the input Kafka topic into a KStream instance.
  val tweets: KStream[Long, String] = builder.stream("twitter")

  // Variant 1: using `mapValues`
  val uppercasedWithMapValue = tweets.

}
