package finrax.clients.twitter.domain.entities.enums

object FilterLevel extends Enumeration {
  type FilterLevel = Value

  val None = Value("none")
  val Low = Value("low")
  val Medium = Value("medium")
}
