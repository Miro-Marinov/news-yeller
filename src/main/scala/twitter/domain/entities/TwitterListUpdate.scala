package twitter.domain.entities

import twitter.domain.entities.enums.Mode.Mode

final case class TwitterListUpdate(description: Option[String] = None,
                                   mode: Option[Mode] = None,
                                   name: Option[String] = None)
