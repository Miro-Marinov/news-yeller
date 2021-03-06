package finrax.clients.twitter.domain.entities.streaming.site

import finrax.clients.twitter.domain.entities.streaming.SiteStreamingMessage

/** New Site Streams connections will receive a control message which may be used to modify the Site Streams connection without reconnecting.
  * See <a href="https://dev.client.twitter.com/streaming/sitestreams/controlstreams" target="_blank">Control Streams for Site Streams</a> for details. Note that this message will not necessarily be the first message delivered on a Site Streams connection.
  * For more information see <a href="https://developer.client.twitter.com/en/docs/tweets/filter-realtime/guides/streaming-message-types" target="_blank">
  *   https://developer.client.twitter.com/en/docs/tweets/filter-realtime/guides/streaming-message-types</a>
  */
final case class ControlMessage(control: ControlMessageInfo) extends SiteStreamingMessage

final case class ControlMessageInfo(control_uri: String)
