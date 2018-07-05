package finrax.http

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.google.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext

@Singleton
class Routes @Inject()(implicit actorSystem: ActorSystem, ec: ExecutionContext, materializer: ActorMaterializer) extends Directives {

  def sse(topic: String, sseSource: Source[ServerSentEvent, NotUsed]): Route = {
    import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
    path(topic) {
      get {
        complete {
          sseSource
        }
      }
    }
  }


}
