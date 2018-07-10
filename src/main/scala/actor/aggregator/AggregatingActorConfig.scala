package finrax.actor.aggregator

import com.google.inject.{Inject, Singleton}
import com.typesafe.config.Config

@Singleton
class AggregatingActorConfig @Inject()(config: Config) {
  val snapshotIntervalMsgs: Int = config.getInt("aggregating-actor.snapshot-interval-msgs")
}
