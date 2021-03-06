package finrax.clients.twitter.domain.entities.streaming.common

import finrax.clients.twitter.domain.entities.streaming.CommonStreamingMessage

/** These messages indicate that a given Tweet has been deleted.
  * Client code must honor these messages by clearing the referenced Tweet from memory and any
  * storage or archive, even in the rare case where a deletion message arrives earlier
  * in the stream that the Tweet it references.
  * For more information see
  * <a href="https://developer.client.twitter.com/en/docs/tweets/filter-realtime/guides/streaming-message-types" target="_blank">
  *   https://developer.client.twitter.com/en/docs/tweets/filter-realtime/guides/streaming-message-types</a>.
  */
final case class StatusDeletionNotice(delete: StatusDeletionNoticeInfo) extends CommonStreamingMessage

final case class StatusDeletionNoticeInfo(status: StatusDeletionId)

final case class StatusDeletionId(id: Long, id_str: String, user_id: Long, user_id_str: String)
