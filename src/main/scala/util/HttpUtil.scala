package finrax.util

import akka.NotUsed
import akka.actor.{ActorSystem, Cancellable}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.stream.Materializer
import akka.stream.scaladsl.{Framing, Source}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.util.{Failure, Success, Try}


object HttpUtil extends LazyLogging {

  def processBody[T](response: HttpResponse)(unmarshal: ByteString => Try[T], delimiter: Option[String])(implicit m: Materializer): Source[T, Any] = {
    val entity = response.entity
    val responseStringBodySource = delimiter.map { del =>
      // Streaming api
      entity.withoutSizeLimit.dataBytes.via(Framing.delimiter(ByteString(del), Int.MaxValue).async)
    } getOrElse {
      // Standard api
      implicit val ec: ExecutionContextExecutor = m.executionContext
      Source.fromFuture(entity.toStrict(5 seconds).map(_.data))
    }

    responseStringBodySource.filter(_.nonEmpty)
      .map(data => unmarshal(data))
      .flatMapConcat {
        case Success(message) =>
          Source.single(message)
        case Failure(cause) =>
          logger.error(s"Got an error while unmarshalling message", cause)
          Source.failed(cause)
      }
  }


  def handleResponse[T](res: Try[HttpResponse])(unmarshal: ByteString => Try[T], delimiter: Option[String] = None)
                       (implicit m: Materializer): Source[T, Any] = res match {
    case Success(response) if response.status.isSuccess =>
      HttpUtil.processBody(response)(unmarshal, delimiter)
    case Success(failureResponse) =>
      val statusCode = failureResponse.status
      import scala.concurrent.duration._
      implicit val ec: ExecutionContextExecutor = m.executionContext
      val eventualErrorData = failureResponse.entity.toStrict(2 seconds)
      val errorData = Await.result(eventualErrorData, 2 seconds)
      logger.error(s"Got an error response with status code: $statusCode and data: $errorData")
      Source.failed(new RuntimeException(s"Got an error response with status code : $statusCode and data: $errorData"))
    case Failure(cause) =>
      logger.error(s"Got an error when attempting to send an http request: $cause")
      Source.failed(cause)
  }

  def requestPollingToStreamOf[T](request: HttpRequest, unmarshal: ByteString => Try[T], pollingIntervalMs: Int)
                                 (implicit system: ActorSystem, m: Materializer): Source[T, Cancellable] = {
    val poolSettings = ConnectionPoolSettings(system)
      .withMaxConnections(1)
      .withPipeliningLimit(1) // TODO: What is this
      .withMaxRetries(0)

    val connPool = Http().superPool[NotUsed](settings = poolSettings)

    Source
      .tick(1 second, pollingIntervalMs millis, request)
      .map(req => (req, NotUsed))
      .via(connPool)
      .map { case (responseTry, _) ⇒ responseTry }
      .flatMapConcat(HttpUtil.handleResponse(_)(unmarshal))
  }
}
