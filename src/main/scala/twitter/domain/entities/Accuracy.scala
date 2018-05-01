package twitter.domain.entities

import twitter.domain.entities.enums.Measure
import twitter.domain.entities.enums.Measure.Measure


final case class Accuracy(amount: Int, unit: Measure) {
  override def toString = s"$amount$unit"
}

object Accuracy {
  val Default = Accuracy(0, Measure.Meter)
}
