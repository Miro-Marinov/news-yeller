package finrax.clients.twitter.domain.entities

final case class LookupRelationship(connections: Seq[String] = Seq.empty,
                                    id: Long,
                                    id_str: String,
                                    name: String,
                                    screen_name: String)
