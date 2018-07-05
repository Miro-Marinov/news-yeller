package finrax.clients.reddit

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import client.reddit.domain.RedditPost
import com.google.inject.Inject
import finrax.clients.reddit.auth.OauthService
import finrax.clients.reddit.config.RedditConfig
import finrax.clients.reddit.domain.{Endpoint, RequestParams, Sorting}
import finrax.serializaiton.JsonSerialization
import finrax.util.HttpUtil
import org.json4s.JsonAST.{JArray, JObject}
import org.json4s.native.JsonMethods._

import scala.collection.mutable
import scala.util.Try

class RedditClient @Inject()(redditConfig: RedditConfig, oauthService: OauthService)
                            (implicit actorSystem: ActorSystem, m: Materializer) {

  def stream(subRedditDisplayName: String, sorting: Sorting.Value = Sorting.HOT, requestParams: RequestParams = RequestParams(mutable.Map())): Source[List[RedditPost], NotUsed] = {
    Source.fromFuture(oauthService.getAccessToken)
      .flatMapConcat {
        token =>
          val contentType = ContentType(MediaTypes.`application/x-www-form-urlencoded`, HttpCharsets.`UTF-8`)
          val entity = FormData(requestParams).toEntity(HttpCharsets.`UTF-8`).withContentType(contentType)
          val url = Endpoint.subreddit(subRedditDisplayName)
          val request = HttpRequest(method = HttpMethods.GET, url)
            .withEntity(entity)
            .addHeader(RawHeader("Authorization", s"Bearer $token"))

          HttpUtil.requestPollingToStreamOf(request, umarshal)
      }
  }


  private def umarshal(data: ByteString) = {
    Try {
      val jsonString = data.utf8String
      val json = parse(jsonString)
      val JObject(posts) = json \ "data" \ "children" \\ "data"
      println(compact(render(JArray(posts map { case (_, v) => v }))))
      import finrax.serializaiton.JsonSerialization.redditFormats
      JArray(posts map { case (_, v) => v }).extract[List[RedditPost]]
    }
  }
}
