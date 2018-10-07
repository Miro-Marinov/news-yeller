package finrax.http

import actor.aggregator.GetState
import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.Timeout
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import finrax.actor.aggregator.State
import serializaiton.JsonSpraySupport

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class NewsController @Inject()(implicit actorSystem: ActorSystem,
                               ec: ExecutionContext,
                               m: ActorMaterializer) extends Directives with LazyLogging with JsonSpraySupport {

  val getNewsHandler = ExceptionHandler {
    case e: Exception =>
      extractUri { uri =>
        logger.error(s"Request to $uri could not be handled normally.", e)
        complete(HttpResponse(InternalServerError, entity = "Failed fetching news."))
      }
  }

  def api(sseSource: Source[ServerSentEvent, NotUsed], aggregator: ActorRef): Route = pathPrefix("news") {
    handleExceptions(getNewsHandler) {
      (get & path("stream")) {
        complete {
          sseSource
        }
      } ~ get {
        complete {
          import akka.pattern.ask
          implicit val askTimeout: Timeout = Timeout(5 seconds)
          (aggregator ? GetState).map(_.asInstanceOf[State])
        }
      }
    }
  }
}
