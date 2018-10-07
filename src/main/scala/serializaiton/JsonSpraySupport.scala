package serializaiton

import java.time.Instant
import java.time.temporal.TemporalAccessor

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import finrax.actor.aggregator.State
import finrax.clients.twitter.domain.entities.{Tweet, User}
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat}
import DateFormats._
import client.reddit.domain.RedditPost
import finrax.client.cryptocontrol.domain.{Article, Coin, SimilarArticle}
import finrax.clients.rssfeed.RssFeedEntry

trait JsonSpraySupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object InstantJsonFormat extends RootJsonFormat[Instant] {
    def write(instant: Instant): JsValue = JsString(finraxDTF.format(instant))

    def read(json: JsValue): Instant = json match {
      case JsString(instant) => finraxDTF.parse(instant, (temporal: TemporalAccessor) => Instant.from(temporal))
      case _ => throw DeserializationException("JsString expected")
    }
  }

  implicit val userFormat: RootJsonFormat[User] = jsonFormat6(User)
  implicit val twitterFormat: RootJsonFormat[Tweet] = jsonFormat9(Tweet.apply)
  implicit val redditPostFormat: RootJsonFormat[RedditPost] = jsonFormat6(RedditPost.apply)
  implicit val rssFeedFormat: RootJsonFormat[RssFeedEntry] = jsonFormat4(RssFeedEntry.apply)
  implicit val cryptoControlCoinFormat: RootJsonFormat[Coin] = jsonFormat4(Coin)
  implicit val cryptoControlSimilarArticleFormat: RootJsonFormat[SimilarArticle] = jsonFormat4(SimilarArticle)
  implicit val cryptoControlArticleFormat: RootJsonFormat[Article] = jsonFormat13(Article.apply)
  implicit val stateFormat: RootJsonFormat[State] = jsonFormat4(State)
}