package twitter.config

import com.google.inject.ImplementedBy
import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}
import twitter.auth.{AccessToken, ConsumerToken}


@Singleton
class AppConfig @Inject()(config: Config) extends TwitterConfig {

  override val consumerToken: ConsumerToken = ConsumerToken(
    config.getString("twitter.auth.consumerTokenKey"),
    config.getString("twitter.auth.consumerTokenSecret")
  )

  override val accessToken: AccessToken = AccessToken(
    config.getString("twitter.auth.accessTokenKey"),
    config.getString("twitter.auth.accessTokenSecret")
  )

  override val restApiEndpoint: String = config.getString("twitter.rest.api")

  override val restMediaEndpoint: String = config.getString("twitter.rest.media")

  override val streamingSiteEndpoint: String = config.getString("twitter.streaming.site")

  override val streamingPublicEndpoint: String = config.getString("twitter.streaming.public")

  override val streamingUserEndpoint: String = config.getString("twitter.streaming.user")

  override val twitterVersion: String = config.getString("twitter.version")
}

@ImplementedBy(classOf[AppConfig])
trait TwitterConfig {
  def consumerToken: ConsumerToken

  def accessToken: AccessToken

  def restApiEndpoint: String

  def restMediaEndpoint: String

  def streamingSiteEndpoint: String

  def streamingPublicEndpoint: String

  def streamingUserEndpoint: String

  def twitterVersion: String
}
