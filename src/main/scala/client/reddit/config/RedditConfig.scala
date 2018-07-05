package finrax.clients.reddit.config

import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}
import collection.JavaConverters._

@Singleton
class RedditConfig @Inject()(config: Config) {
  val clientId: String = config.getString("reddit.clientId")
  val clientSecret: String = config.getString("reddit.clientSecret")
  val userAgent: String = config.getString("reddit.userAgent")
  val username: String = config.getString("reddit.username")
  val password: String = config.getString("reddit.password")

  val subs:List[String] = config.getStringList("reddit.subs").asScala.toList
}
