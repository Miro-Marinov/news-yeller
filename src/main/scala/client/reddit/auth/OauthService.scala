package finrax.clients.reddit.auth

import java.util.Base64

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.Materializer
import akka.util.ByteString
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import finrax.clients.reddit.config.RedditConfig
import finrax.clients.reddit.domain.Endpoint
import org.json4s._
import org.json4s.native.JsonMethods._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

@Singleton
class OauthService @Inject()(redditConfig: RedditConfig)(implicit actorSystem: ActorSystem, m: Materializer) extends LazyLogging {

  def getAccessToken: Future[String] = {
    import redditConfig._
    val formParams = Map(
      "grant_type" -> "password",
      "username" -> username,
      "password" -> password)
    val contentType = ContentType(MediaTypes.`application/x-www-form-urlencoded`, HttpCharsets.`UTF-8`)
    val entity = FormData(formParams).toEntity(HttpCharsets.`UTF-8`).withContentType(contentType)
    val request = HttpRequest(method = HttpMethods.POST, Endpoint.access_token)
      .withEntity(entity)
      .addHeader(RawHeader(s"Authorization", s"Basic ${genSignature(clientId, clientSecret)}"))

    implicit val ec: ExecutionContextExecutor = m.executionContext
    val eventualResponseEntity = Http(actorSystem).singleRequest(request).flatMap { response =>
      response.entity.toStrict(5 seconds).map(_.data)
    } recoverWith {
      case e =>
        logger.error("Error when authenticating to Reddit", e)
        Future.failed(e)
    }
    eventualResponseEntity map unmarshalAccessToken
  }

  private def unmarshalAccessToken(data: ByteString): String = {
    import finrax.serializaiton.JsonSerialization.commonFormats
    compact(render(parse(data.utf8String) \ "access_token")) replaceAll("\"", "")
  }

  private def genSignature(key: String, secret: String): String = {
    val str = key + ":" + secret
    Base64.getEncoder.encodeToString(str.getBytes("UTF-8"))
  }
}