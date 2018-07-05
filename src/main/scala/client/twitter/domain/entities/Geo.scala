package finrax.clients.twitter.domain.entities

final case class Geo(coordinates: Seq[Double] = Seq.empty, `type`: String)
