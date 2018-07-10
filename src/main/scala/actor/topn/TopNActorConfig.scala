package finrax.actor.topn

import com.google.inject.{Inject, Singleton}
import com.typesafe.config.Config

@Singleton
class TopNActorConfig @Inject()(config: Config) {
  val snapshotIntervalMsgs: Int = config.getInt("top-n-actor.snapshot-interval-msgs")
  val capacity: Int = config.getInt("top-n-actor.capacity")
}