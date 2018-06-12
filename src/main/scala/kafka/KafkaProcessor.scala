package kafka

import java.util.Properties

import com.google.inject.Singleton
import com.lightbend.kafka.scala.streams.DefaultSerdes._
import com.lightbend.kafka.scala.streams.ImplicitConversions._
import com.lightbend.kafka.scala.streams.KStreamS
import javax.inject.Inject
import kafka.config.KafkaConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig}
import org.json4s.native.Serialization
import serializaiton.JsonSupport
import twitter.domain.entities.Tweet

import scala.collection.mutable.ArrayBuffer

@Singleton
class KafkaProcessor @Inject()(kafkaConfig: KafkaConfig) extends JsonSupport {
  val bootstrapServers: String = kafkaConfig.bootstrapServers
  val builder = new StreamsBuilder
  val n = 15
  val streamingConfig: Properties = {
    val settings = new Properties
    settings.put(StreamsConfig.APPLICATION_ID_CONFIG, "news-yeller")
    settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    // Specify default (de)serializers for record keys and for record values.
    settings.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
    settings.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
    settings
  }

  // Read the input Kafka topic into a KStream instance.
  val tweets: KStreamS[String, String] = builder.stream[String, String]("tweets")

  val aggregated: KStreamS[String, String] = tweets
/*    .filter((key, value) => {
      val tweet = Serialization.read[Tweet](value)
      tweet.retweet_count > -1
    })*/
    // TODO: Code smell - used to aggregate the entire stream.
    .groupBy((_, v) => "")
    // TODO: Should we use a thread-safe collection?
    .aggregate(
    // TODO: Extract to config.
    () => Serialization.write(List.empty[Tweet]),
    (_: String, value: String, aggr: String) => aggregateTopN(n)(value, aggr))
    .mapValues(tweets => {
      println(tweets)
      tweets
    })
    .toStream
  //    .to("aggregated-tweets")

  def run(): Unit = {
    val streams = new KafkaStreams(builder.build(), streamingConfig)
    streams.start()
  }

  private def aggregateTopN(n: Int)(value: String, aggr: String): String = {
    // TODO: Add Try[] and recover if a failure occurs during deserialization.
    // TODO: Use priority queue
    val tweet = Serialization.read[Tweet](value)
    var aggrInternal = Serialization.read[ArrayBuffer[Tweet]](aggr)
    val length = aggrInternal.length
    if (length < n) {
      aggrInternal = aggrInternal += tweet
      Serialization.write(aggrInternal.sortBy(_.retweet_count)(Ordering[Long].reverse).toList)
    } else {
      val last = aggrInternal(length - 1)
      if (last.retweet_count < tweet.retweet_count) {
        aggrInternal.update(length - 1, tweet)
      }
      Serialization.write(aggrInternal.sortBy(_.retweet_count)(Ordering[Long].reverse).toList)
    }
  }

}
