package finrax.clients.twitter.domain.entities

final case class Url(indices: Seq[Int] = Seq.empty, url: String, display_url: String, expanded_url: String)
