package finrax.clients.twitter

import akka.{Done, NotUsed}
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.settings._
import akka.stream.scaladsl.{Source, _}
import akka.stream.{ActorMaterializer, KillSwitches, Materializer, UniqueKillSwitch}
import akka.util.{ByteString, Timeout}
import com.typesafe.scalalogging.LazyLogging
import finrax.actor.topn.{TopNActor, TopNActorConfig}
import finrax.clients.twitter.auth.OAuthHeaderGenerator
import finrax.clients.twitter.config.TwitterConfig
import finrax.clients.twitter.domain.entities.Tweet
import finrax.serializaiton.JsonSerialization
import finrax.util.{ActorUtil, HttpUtil}
import javax.inject.Inject
import org.json4s.native.Serialization

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try

class TwitterClient @Inject()(twitterConfig: TwitterConfig,
                              authHeaderGenerator: OAuthHeaderGenerator,
                              topNActorConfig: TopNActorConfig)
                             (implicit actorSystem: ActorSystem, m: ActorMaterializer) extends LazyLogging {

  import twitterConfig._

  private val statusesUrl = s"$streamingPublicEndpoint/$twitterVersion/statuses"

  def startTwitterStream(aggregator: ActorRef): Future[Done] = {
    val twitterTopNActorProps = TopNActor.props[Tweet, String](aggregator, "twitter", topNActorConfig)(tweet => tweet.id_str)
    val supervisorProps = ActorUtil.backOffSupervisorProps("twitterActor", twitterTopNActorProps)
    val twitterActorSupervisor = actorSystem.actorOf(supervisorProps, name = "twitterActorSupervisor")

    getStatusesFilterStream
      .mapAsync(5) { tweet =>
        import akka.pattern.ask
        implicit val askTimeout: Timeout = Timeout(5 seconds)
        twitterActorSupervisor ? tweet
      } runWith Sink.ignore
  }

  def getStatusesFilterStream: Source[Tweet, UniqueKillSwitch] = {
    val filter: StatusFilter = StatusFilter(tracks = twitterConfig.tracks, follow = twitterConfig.followed)

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

    def toTweetStream: Source[Tweet, UniqueKillSwitch] = { // format: ON

      val scheme = request.uri.scheme
      val host = request.uri.authority.host.toString
      val port = request.uri.effectivePort

      val poolSettings = ConnectionPoolSettings.default
        .withPipeliningLimit(1) // TODO: What is this
        .withMaxRetries(0)

      val httpFlow: Flow[(HttpRequest, NotUsed), (Try[HttpResponse], NotUsed), HostConnectionPool] =
        if (scheme == "https")
          Http().newHostConnectionPoolHttps(host, port, settings = poolSettings)
        else
          Http().newHostConnectionPool(host, port, poolSettings)

      Source.single(request)
        .viaMat(KillSwitches.single)(Keep.right)
        // the NotUsed is just a placeholder - in general an object is used to associate requests
        // with responses if we have multiple requests and out of order (in our case there can be only one request at a time)
        .map(request ⇒ (request, NotUsed))
        .via(httpFlow)
        .map { case (responseTry, _) ⇒ responseTry }
        .flatMapConcat(HttpUtil.handleResponse(_)(unmarshalStream, delimiter = Some("\r\n")))
    }

    private def unmarshalStream(data: ByteString): Try[Tweet] = {
      val json = data.utf8String
      import JsonSerialization.twitterFormats
      //TODO: Deal with the {"limit":{"track":16,"timestamp_ms":"1530816027847"} message when too many tweets are being streamed
      Try(Serialization.read[Tweet](json))
    }
  }

}
