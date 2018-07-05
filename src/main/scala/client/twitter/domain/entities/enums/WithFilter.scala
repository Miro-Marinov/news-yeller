package finrax.clients.twitter.domain.entities.enums

object WithFilter extends Enumeration {
  type WithFilter = Value

  val User = Value("user")
  val Followings = Value("followings")
}
