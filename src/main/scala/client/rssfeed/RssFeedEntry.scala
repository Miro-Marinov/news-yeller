package finrax.clients.rssfeed

import java.time.Instant

case class RssFeedEntry(pubDate: Instant, title: String, categories: List[String], link: String)

object RssFeedEntry {
  implicit val ord: Ordering[RssFeedEntry] = Ordering.by({
    feedEntry: RssFeedEntry => feedEntry.pubDate
  })
}


