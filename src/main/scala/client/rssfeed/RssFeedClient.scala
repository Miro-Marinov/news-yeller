package finrax.clients.rssfeed

import akka.actor.{ActorSystem, Cancellable}
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.Uri
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import finrax.util.HttpUtil
import javax.inject.Inject
import org.json4s.JsonAST.{JArray, JObject}
import org.json4s.Xml.toJson
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization

import scala.util.Try
import scala.xml.XML


case class RssFeedClient @Inject()(implicit system: ActorSystem, m: Materializer) extends LazyLogging {

  def stream(url: String): Source[List[RssFeedEntry], Cancellable] = {
    val request = RequestBuilding.Get(Uri(url))
    HttpUtil.requestPollingToStreamOf(request, unmarshalXML)
  }

  private def unmarshalXML(byteData: ByteString) = {
    Try {
      // Remove comments
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
        itemWithMergedCategories = JObject("categories" -> JArray(categories) :: itemWithNoCategories)
      } yield itemWithMergedCategories
      val jsString = compact(render(JArray(itemsWithMergedCategories)))
      import finrax.serializaiton.JsonSerialization.redditFormats
      Serialization.read[List[RssFeedEntry]](jsString).sorted.take(15)
    }
  }
}

