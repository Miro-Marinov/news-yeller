package http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.{Directives, Route}
import akka.kafka.ConsumerMessage
import akka.stream.ActorMaterializer
import com.google.inject.{Inject, Singleton}
import kafka.KafkaConsumer

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class Routes @Inject()(implicit actorSystem: ActorSystem, ec: ExecutionContext, materializer: ActorMaterializer) extends Directives {

  def sse(topic: String)(implicit kafkaConsumer: KafkaConsumer): Route = {
    import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._

    path(topic) {
      get {
        complete {
          kafkaConsumer.source(topic)
            .map(tuple => (commitOffsetAndReturnSSE _).tupled(tuple))
            .keepAlive(5.second, () => ServerSentEvent.heartbeat)
        }
      }
    }
  }

  def commitOffsetAndReturnSSE(offset: ConsumerMessage.CommittableOffset, data: String) = {
    val sse = ServerSentEvent(data)
    offset.commitScaladsl()
    sse
  }
}
