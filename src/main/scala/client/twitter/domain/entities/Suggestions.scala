package finrax.clients.twitter.domain.entities

final case class Suggestions(name: String, slug: String, size: Int, users: Seq[User] = Seq.empty)
