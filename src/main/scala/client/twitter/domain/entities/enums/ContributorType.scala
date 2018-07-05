package finrax.clients.twitter.domain.entities.enums

object ContributorType extends Enumeration {
  type ContributorType = Value

  val All = Value("all")
  val Following = Value("following")
  val `None` = Value("none")
}
