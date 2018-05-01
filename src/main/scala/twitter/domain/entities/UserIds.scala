package twitter.domain.entities

final case class UserIds(ids: Seq[Long] = Seq.empty, next_cursor: Long, previous_cursor: Long)
