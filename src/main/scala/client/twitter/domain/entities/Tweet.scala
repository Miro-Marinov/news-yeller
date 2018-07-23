package finrax.clients.twitter.domain.entities

import java.time.Instant


final case class Tweet(created_at: Instant,
                       favorite_count: Int = 0,
                       id_str: String,
                       lang: Option[String] = None,
                       possibly_sensitive: Boolean = false,
                       retweet_count: Long = 0,
                       source: String,
                       text: String,
                       user: Option[User] = None) {
}

object Tweet {
  implicit val ord: Ordering[Tweet] = Ordering.by({
    tweet: Tweet => (tweet.retweet_count, tweet.favorite_count, tweet.created_at)
  })
}
