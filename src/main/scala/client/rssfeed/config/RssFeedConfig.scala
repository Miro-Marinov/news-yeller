package client.rssfeed.config

import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}

import scala.collection.JavaConverters._

@Singleton
class RssFeedConfig @Inject()(config: Config) {
  val rssFeeds:List[String] = config.getStringList("rss.feeds").asScala.toList
  val pollingInterval:Int = config.getInt("rss.polling-interval")
}
