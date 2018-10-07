package finrax.clients.twitter.config

import com.typesafe.config.Config
import finrax.clients.twitter.auth.{AccessToken, ConsumerToken}
import javax.inject.{Inject, Singleton}
import collection.JavaConverters._


@Singleton
class TwitterConfig @Inject()(config: Config) {

  val consumerToken: ConsumerToken = ConsumerToken(
    config.getString("twitter.auth.consumerTokenKey"),
    config.getString("twitter.auth.consumerTokenSecret")
  )

  val accessToken: AccessToken = AccessToken(
    config.getString("twitter.auth.accessTokenKey"),
    config.getString("twitter.auth.accessTokenSecret")
  )

  val restApiEndpoint: String = config.getString("twitter.rest.api")

  val restMediaEndpoint: String = config.getString("twitter.rest.media")

  val streamingSiteEndpoint: String = config.getString("twitter.streaming.site")

  val streamingPublicEndpoint: String = config.getString("twitter.streaming.public")

  val streamingUserEndpoint: String = config.getString("twitter.streaming.user")

  val twitterVersion: String = config.getString("twitter.version")
  
  val followed: List[Long] = config.getLongList("twitter.followed").asScala.map(Long2long).toList

  val followedStr: List[String] = config.getStringList("twitter.followedstr").asScala.toList

  val tracks: List[String] = config.getStringList("twitter.tracks").asScala.toList

  val pollingInterval: Long = config.getLong("twitter.polling-interval")

  val queryRange: Long = config.getLong("twitter.query-range")
}