package finrax.clients.reddit

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.{ByteString, Timeout}
import client.reddit.domain.RedditPost
import com.google.inject.Inject
import finrax.actor.topn.{TopNActor, TopNActorConfig}
import finrax.clients.reddit.auth.OauthService
import finrax.clients.reddit.config.RedditConfig
import finrax.clients.reddit.domain.{Endpoint, RequestParams, Sorting}
import finrax.util.{ActorUtil, HttpUtil}
import org.json4s.JsonAST.{JArray, JObject}
import org.json4s.native.JsonMethods._

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class RedditClient @Inject()(redditConfig: RedditConfig, oauthService: OauthService, topNActorConfig: TopNActorConfig)
                            (implicit actorSystem: ActorSystem, m: Materializer) {

  // TODO Sorting.HOT is unused
  def stream(subRedditDisplayName: String, sorting: Sorting.Value = Sorting.HOT, requestParams: RequestParams = RequestParams(mutable.Map())): Source[List[RedditPost], NotUsed] = {
    Source.fromFuture(oauthService.getAccessToken)
      .flatMapConcat {
        case Success(token) =>
          val contentType = ContentType(MediaTypes.`application/x-www-form-urlencoded`, HttpCharsets.`UTF-8`)
          val entity = FormData(requestParams).toEntity(HttpCharsets.`UTF-8`).withContentType(contentType)
          val url = Endpoint.subreddit(subRedditDisplayName)
          val request = HttpRequest(method = HttpMethods.GET, url)
            .withEntity(entity)
            .addHeader(RawHeader("Authorization", s"Bearer $token"))
          HttpUtil.requestPollingToStreamOf(request, unmarshal, redditConfig.pollingIntervalMs)
        case Failure(_) =>
          Source.empty
      }
  }

  def pollAndSendToAggregator(aggregator: ActorRef): Unit = {
    val redditTopNActorProps = TopNActor.props[RedditPost, String](aggregator, "reddit", topNActorConfig)(entry => entry.permalink)
    val supervisorProps = ActorUtil.backOffSupervisorProps("redditActor", redditTopNActorProps)
    val redditActorSupervisor = actorSystem.actorOf(supervisorProps, name = "redditActorSupervisor")

    redditConfig.subs.foreach(sub =>
      stream(sub).mapAsync(5) { redditPosts =>
        import akka.pattern.ask
        implicit val askTimeout: Timeout = Timeout(5 seconds)
        redditActorSupervisor ? redditPosts
      } runWith Sink.ignore)
  }

  private def unmarshal(data: ByteString) = {
    Try {
      val jsonString = data.utf8String
      val json = parse(jsonString)
      val JObject(posts) = json \ "data" \ "children" \\ "data"
      import finrax.serializaiton.JsonSupport.redditFormats
      JArray(posts map { case (_, v) => v }).extract[List[RedditPost]].filterNot(_.stickied)
    }
  }
}
