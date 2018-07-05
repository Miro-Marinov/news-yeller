package finrax.clients.twitter.domain.entities.streaming

final case class StreamingUpdate(streamingEvent: StreamingMessage)

trait StreamingMessage

trait CommonStreamingMessage extends UserStreamingMessage with SiteStreamingMessage
trait UserStreamingMessage extends StreamingMessage
trait SiteStreamingMessage extends StreamingMessage
