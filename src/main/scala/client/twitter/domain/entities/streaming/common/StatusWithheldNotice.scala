package finrax.clients.twitter.domain.entities.streaming.common

import finrax.clients.twitter.domain.entities.streaming.CommonStreamingMessage

/** These events contain an id field indicating the status ID, a user_id indicating the user,
  * and a collection of withheld_in_countries uppercase two-letter country codes.
  * For more information see
  * <a href="https://developer.client.twitter.com/en/docs/tweets/filter-realtime/guides/streaming-message-types" target="_blank">
  *   https://developer.client.twitter.com/en/docs/tweets/filter-realtime/guides/streaming-message-types</a>.
  */
final case class StatusWithheldNotice(status_withheld: StatusWithheldId) extends CommonStreamingMessage

final case class StatusWithheldId(id: Long, user_id: Long, withheld_in_countries: List[String])
