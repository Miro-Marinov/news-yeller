package finrax.clients.twitter.domain.entities

final case class TwitterLists(lists: Seq[TwitterList] = Seq.empty, next_cursor: Long, previous_cursor: Long)
