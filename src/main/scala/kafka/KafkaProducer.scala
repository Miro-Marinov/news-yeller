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

  def twitter(keyF: Tweet => String): Sink[Tweet, NotUsed] =
    Flow[Tweet]
      .map { tweet =>
        val key = keyF(tweet)
        val json = Serialization.write(tweet)
        println(json)
        new ProducerRecord[String, String]("tweets", key, json)
      }
      .to(Producer.plainSink(kafkaConfig.producerSettings))
}
