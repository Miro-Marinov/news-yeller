package twitter.domain.entities.enums

object TweetMode extends Enumeration {
  type TweetMode = Value

  val Extended = Value("extended")
  val Classic = Value("")
}
