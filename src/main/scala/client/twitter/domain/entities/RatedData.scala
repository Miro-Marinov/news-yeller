package finrax.clients.twitter.domain.entities

/** A wrapper for data that have rate limitation.
  * Have a look at `rate_limit` for more information on current rates usage.
  */
final case class RatedData[T](rate_limit: RateLimit, data: T)
