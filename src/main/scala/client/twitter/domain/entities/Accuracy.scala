package finrax.clients.twitter.domain.entities

import finrax.clients.twitter.domain.entities.enums.Measure
import finrax.clients.twitter.domain.entities.enums.Measure.Measure


final case class Accuracy(amount: Int, unit: Measure) {
  override def toString = s"$amount$unit"
}

object Accuracy {
  val Default = Accuracy(0, Measure.Meter)
}
