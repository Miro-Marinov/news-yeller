package kafka

import akka.NotUsed
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Flow, Sink}
import com.google.inject.{Inject, Singleton}
import kafka.config.KafkaConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.json4s.native.Serialization
import serializaiton.JsonSupport
import twitter.domain.entities.Tweet


@Singleton
class KafkaProducer @Inject()(kafkaConfig: KafkaConfig) extends JsonSupport {

  def twitter: Sink[Tweet, NotUsed] =
    Flow[Tweet]
      .map { tweet =>
        val key = tweet.id
        val json = Serialization.write(tweet)
        new ProducerRecord[Long, String]("twitter", key, json)
      }
      .to(Producer.plainSink(kafkaConfig.producerSettings))
}
