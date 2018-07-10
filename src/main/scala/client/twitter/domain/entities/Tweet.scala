package finrax.clients.twitter.domain.entities

import java.time.Instant


final case class Tweet(matching_rules: Seq[MatchingRule] = Seq.empty,
                       coordinates: Option[Coordinates] = None,
                       created_at: Instant,
                       current_user_retweet: Option[TweetId] = None,
                       entities: Option[Entities] = None,
                       extended_entities: Option[Entities] = None,
                       extended_tweet: Option[ExtendedTweet] = None,
                       favorite_count: Int = 0,
                       favorited: Boolean = false,
                       filter_level: Option[String] = None,
                       geo: Option[Geo] = None,
                       id: Long,
                       id_str: String,
                       is_quote_status: Boolean = false,
                       lang: Option[String] = None,
                       place: Option[GeoPlace] = None,
                       possibly_sensitive: Boolean = false,
                       retweet_count: Long = 0,
                       retweeted: Boolean = false,
                       retweeted_status: Option[Tweet] = None,
                       source: String,
                       text: String,
                       truncated: Boolean = false,
                       display_text_range: Option[Seq[Int]] = None,
                       user: Option[User] = None,
                       withheld_copyright: Boolean = false,
                       withheld_in_countries: Seq[String] = Seq.empty,
                       withheld_scope: Option[String] = None,
                       metadata: Option[StatusMetadata] = None) {
}

object Tweet {
  implicit val ord: Ordering[Tweet] = Ordering.by({
    tweet: Tweet => (tweet.retweet_count, tweet.favorite_count, tweet.created_at)
  })
}
