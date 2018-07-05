package finrax.clients.twitter.domain.entities.streaming.user

import java.util.Date

import finrax.clients.twitter.domain.entities.{Tweet, TwitterList, User}
import finrax.clients.twitter.domain.entities.enums.EventCode
import finrax.clients.twitter.domain.entities.enums.SimpleEventCode.SimpleEventCode
import finrax.clients.twitter.domain.entities.enums.TweetEventCode.TweetEventCode
import finrax.clients.twitter.domain.entities.enums.TwitterListEventCode.TwitterListEventCode
import finrax.clients.twitter.domain.entities.streaming.UserStreamingMessage

/** Notifications about non-Tweet events are also sent over a user stream.
  * The values present will be different based on the type of event.
  * For more information see
  * <a href="https://developer.client.twitter.com/en/docs/tweets/filter-realtime/guides/streaming-message-types" target="_blank">
  *   https://developer.client.twitter.com/en/docs/tweets/filter-realtime/guides/streaming-message-types</a>.
  */
abstract class Event[T](created_at: Date, event: EventCode#Value, target: User, source: User, target_object: Option[T])
    extends UserStreamingMessage

final case class SimpleEvent(created_at: Date,
                             event: SimpleEventCode,
                             target: User,
                             source: User,
                             target_object: Option[String])
    extends Event(created_at, event, target, source, target_object)

final case class TweetEvent(created_at: Date, event: TweetEventCode, target: User, source: User, target_object: Tweet)
    extends Event(created_at, event, target, source, Some(target_object))

final case class TwitterListEvent(created_at: Date,
                                  event: TwitterListEventCode,
                                  target: User,
                                  source: User,
                                  target_object: TwitterList)
    extends Event(created_at, event, target, source, Some(target_object))
