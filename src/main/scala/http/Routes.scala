package http

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.google.inject.{Inject, Singleton}
import kafka.KafkaConsumer

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class Routes @Inject()(implicit actorSystem: ActorSystem, ec: ExecutionContext, materializer: ActorMaterializer) extends Directives {

  def sse(topic: String, source: Source[ServerSentEvent, NotUsed])(implicit kafkaConsumer: KafkaConsumer): Route = {
    import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
    path(topic) {
      get {
        complete {
          source
            .map(tweet => ServerSentEvent(tweet.toString))
            .keepAlive(5.second, () => ServerSentEvent.heartbeat)
        }
      }
    }
  }


}
