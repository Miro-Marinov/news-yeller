package finrax.clients.reddit.domain

import finrax.clients.reddit.domain

object Sorting extends Enumeration {
  type Sorting = Value
  val TOP: domain.Sorting.Value = Value("top")
  val HOT: domain.Sorting.Value = Value("hot")
  val NEWEST: domain.Sorting.Value = Value("newest")
  val RANDOM: domain.Sorting.Value = Value("random")
  val RISING: domain.Sorting.Value = Value("rising")
  val CONTROVERSIAL: domain.Sorting.Value = Value("controversial")
}