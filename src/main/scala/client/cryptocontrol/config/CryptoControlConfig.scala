package client.cryptocontrol.config

import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}

@Singleton
class CryptoControlConfig @Inject()(config: Config) {
  val pollingIntervalMs: Int = config.getInt("cryptocontrol.polling-interval")
}
