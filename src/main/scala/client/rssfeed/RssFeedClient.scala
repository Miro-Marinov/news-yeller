package finrax.clients.rssfeed

import akka.actor.{ActorRef, ActorSystem, Cancellable}
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.Uri
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.{ByteString, Timeout}
import client.rssfeed.config.RssFeedConfig
import com.typesafe.scalalogging.LazyLogging
import finrax.actor.topn.{TopNActor, TopNActorConfig}
import finrax.clients.twitter.domain.entities.Tweet
import finrax.util.{ActorUtil, HttpUtil}
import javax.inject.Inject
import org.json4s.JsonAST.{JArray, JObject}
import org.json4s.Xml.toJson
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization

import scala.util.Try
import scala.xml.XML
import scala.concurrent.duration._

case class RssFeedClient @Inject()(rssFeedConfig: RssFeedConfig, topNActorConfig: TopNActorConfig)(implicit actorSystem: ActorSystem, m: Materializer) extends LazyLogging {

  def pollAndSendToAggregator(aggregator: ActorRef): Unit = {
    val rssFeedTopNActorProps = TopNActor.props[RssFeedEntry, String](aggregator, "rssFeed", topNActorConfig)(entry => entry.link)
    val supervisorProps = ActorUtil.backOffSupervisorProps("rssFeedActor", rssFeedTopNActorProps)
    val rssFeedSupervisor = actorSystem.actorOf(supervisorProps, name = "rssFeedActorSupervisor")

    rssFeedConfig.rssFeeds.foreach(feedUrl =>
      stream(feedUrl).mapAsync(5) { redditPosts =>
        import akka.pattern.ask
        implicit val askTimeout: Timeout = Timeout(5 seconds)
        rssFeedSupervisor ? redditPosts
      } runWith Sink.ignore)
  }

  def stream(url: String): Source[List[RssFeedEntry], Cancellable] = {
    val request = RequestBuilding.Get(Uri(url))
    HttpUtil.requestPollingToStreamOf(request, unmarshalXML, rssFeedConfig.pollingInterval)
  }

  private def unmarshalXML(byteData: ByteString) = {
    Try {
      // Remove comments from the XML
      val xmlWithNoComments = byteData.utf8String.replaceAll("<!--[\\s\\S]*?-->", "")
      val xml = XML.loadString(xmlWithNoComments)
      val rss = toJson(xml)
      // TODO: Get all categories: FIX
      val JObject(items) = rss \\ "item"
      // Transform to array of items
      val arrItems = items map { case (_, value) => value }
      val itemsWithMergedCategories = for {
        item <- arrItems
        categories = item.filterField { case (k, _) => k == "category" } map { case (_, v) => v }
        JObject(itemWithNoCategories) = item.removeField { case (k, _) => k == "category" }
        // Merge the categories into a single list field (instead this is needed because XML allows for multiple elements wit the same
        // tag which will be translated into multiple equivalent keys in the JSON
        itemWithMergedCategories = JObject("categories" -> JArray(categories) :: itemWithNoCategories)
      } yield itemWithMergedCategories
      val jsString = compact(render(JArray(itemsWithMergedCategories)))
      import finrax.serializaiton.JsonSupport.rssFeedFormats
      Serialization.read[List[RssFeedEntry]](jsString).sorted.take(15)
    }
  }
}

