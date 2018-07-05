package finrax.clients.twitter.enums

object FilterLevel extends Enumeration {
  type FilterLevel = Value
  val None: FilterLevel.Value = Value("none")
  val Low: FilterLevel.Value = Value("low")
  val Medium: FilterLevel.Value = Value("medium")
}