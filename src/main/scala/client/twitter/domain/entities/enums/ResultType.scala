package finrax.clients.twitter.domain.entities.enums

object ResultType extends Enumeration {
  type ResultType = Value

  val Mixed = Value("mixed")
  val Recent = Value("recent")
  val Popular = Value("popular")
}
