package twitter.domain.entities

final case class LookupMapped(id: Map[String, LookupTweet] = Map.empty)
