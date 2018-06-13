package twitter

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.settings._
import akka.stream.scaladsl.{Framing, Source, _}
import akka.stream.{ActorMaterializer, KillSwitches, Materializer, UniqueKillSwitch}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import org.json4s.native.Serialization
import serializaiton.JsonSupport
import twitter.auth.OAuthHeaderGenerator
import twitter.config.TwitterConfig
import twitter.domain.entities.Tweet

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.util.{Failure, Success, Try}

class TwitterClient @Inject()(twitterConfig: TwitterConfig,
                              authHeaderGenerator: OAuthHeaderGenerator)
                             (implicit system: ActorSystem, m: ActorMaterializer) extends LazyLogging with JsonSupport {

  import twitterConfig._

  private val statusesUrl = s"$streamingPublicEndpoint/$twitterVersion/statuses"

  def getStatusesFilterStream(filter: StatusFilter): Source[Tweet, UniqueKillSwitch] = {
    val urlEncodedParams = filter.asUrlEncodedParams
    val contentType = ContentType(MediaTypes.`application/x-www-form-urlencoded`, HttpCharsets.`UTF-8`)
    val httpEntity = HttpEntity(urlEncodedParams).withContentType(contentType)
    val request = HttpRequest(method = HttpMethods.POST, s"$statusesUrl/filter.json")
      .withEntity(httpEntity)
      .withTwitterAuthHeader(filter)
    request.toTweetStream
  }

  private implicit class TwitterAuthenticatedLongLivedRequest(request: HttpRequest) {
    def withTwitterAuthHeader(filter: StatusFilter): HttpRequest =
      request.addHeader(authHeaderGenerator.getAuthHeader(request, filter, twitterConfig.accessToken, twitterConfig.consumerToken))

    def toTweetStream(implicit system: ActorSystem, m: Materializer): Source[Tweet, UniqueKillSwitch] = { // format: ON

      val scheme = request.uri.scheme
      val host = request.uri.authority.host.toString
      val port = request.uri.effectivePort

      val poolSettings = ConnectionPoolSettings(system)
        .withMaxConnections(1)
        .withPipeliningLimit(1) // TODO: What is this
        .withMaxRetries(0) // TODO: What is this

      val httpFlow: Flow[(HttpRequest, String), (Try[HttpResponse], String), HostConnectionPool] =
        if (scheme == "https")
          Http().newHostConnectionPoolHttps(host, port, settings = poolSettings)
        else
          Http().newHostConnectionPool(host, port, poolSettings)

      Source.single(request)
        .viaMat(KillSwitches.single)(Keep.right)
        // the UUID is just a placeholder - in general it is used to associate requests
        // with responses (in our case there can be only one request at a time)
        .map(request ⇒ (request, UUID.randomUUID().toString))
        .via(httpFlow)
        .map { case (responseTry, _) ⇒ responseTry }
        .flatMapConcat {
          case Success(response) if response.status.isSuccess =>
            processBody(response)
          case Success(failureResponse) =>
            val statusCode = failureResponse.status
            import scala.concurrent.duration._
            implicit val ec: ExecutionContextExecutor = m.executionContext
            val eventualErrorData = failureResponse.entity.toStrict(2 seconds)
            val errorData = Await.result(eventualErrorData, 2 seconds)
            logger.error(s"Got an error response with status code: $statusCode and data: $errorData")
            Source.failed(new RuntimeException(s"Got an error response with status code : $statusCode and data: $errorData"))
          case Failure(cause) =>
            logger.error(s"Got an error when attempting to connect to: ${request.uri.toString()}: $cause")
            Source.failed(cause)
        }
    }

    private def processBody(response: HttpResponse): Source[Tweet, Any] =
      response.entity.withoutSizeLimit.dataBytes
        .via(Framing.delimiter(ByteString("\r\n"), Int.MaxValue).async) // TODO MaxValue?
        .filter(_.nonEmpty)
        .map(data => unmarshalStream(data))
        .flatMapConcat {
          case Success(message) =>
            Source.single(message)
          case Failure(cause) =>
            logger.error(s"Got an error while unmarshalling message", cause)
            Source.failed(cause)
        }

    private def unmarshalStream(data: ByteString): Try[Tweet] = {
      val json = data.utf8String
      Try(Serialization.read[Tweet](json))
    }
  }

}
