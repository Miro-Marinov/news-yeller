package finrax.serializaiton

import java.text.SimpleDateFormat
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.TemporalAccessor
import java.time.{Duration, Instant, ZoneId, ZoneOffset}
import java.util.{Locale, UUID}

import org.json4s.JsonAST.{JNull, JString}
import org.json4s.{CustomKeySerializer, CustomSerializer, DefaultFormats, Formats}

import scala.util.Try
import serializaiton.DateFormats._

object JsonSupport {
  object NoneSerializer extends CustomSerializer[Option[_]](_ => ( {
    case JNull => None
  }, {
    case None => JNull
  }))

  object RedditInstantSerializer extends CustomSerializer[Instant](_ => ( {
    case JString(x) => redditDTF.parse(x, (temporal: TemporalAccessor) => Instant.from(temporal))
  }, {
    case x: Instant => JString(redditDTF.format(x))
  }))

  object RssFeedInstantSerializer extends CustomSerializer[Instant](_ => ( {
    case JString(x) =>
      Try {
        redditDTF.parse(x, (temporal: TemporalAccessor) => Instant.from(temporal))
      }.getOrElse(blockChainBlogRssFeed.parse(x, (temporal: TemporalAccessor) => Instant.from(temporal)))
  }, {
    case x: Instant => JString(redditDTF.format(x))
  }))

  object TwitterInstantSerializer extends CustomSerializer[Instant](_ => ( {
    case JString(x) => twitterDTF.parse(x, (temporal: TemporalAccessor) => Instant.from(temporal))
  }, {
    case x: Instant => JString(redditDTF.format(x))
  }))

  object JavaDurationSerializer extends CustomSerializer[Duration](_ => ( {
    case JString(x) => Duration.parse(x)
  }, {
    case x: Duration => JString(x.toString)
  }))

  object ScalaDurationSerializer extends CustomSerializer[scala.concurrent.duration.Duration](_ => ( {
    case JString(x) => scala.concurrent.duration.Duration(x)
  }, {
    case x: scala.concurrent.duration.Duration => JString(Duration.ofMillis(x.toMillis).toString)
  }))

  object UUIDSerializer extends CustomKeySerializer[UUID](_ => ( {
    case s: String => UUID.fromString(s)
  }, {
    case x: UUID => x.toString
  }))

  implicit val commonFormats: Formats = new DefaultFormats {
    override def dateFormatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
  } ++ Seq(NoneSerializer, JavaDurationSerializer, ScalaDurationSerializer)

  implicit val finraxFormats: Formats = commonFormats ++ Seq(RedditInstantSerializer)

  implicit val redditFormats: Formats = commonFormats ++ Seq(RedditInstantSerializer)

  implicit val rssFeedFormats: Formats = commonFormats ++ Seq(RssFeedInstantSerializer)

  implicit val twitterFormats: Formats = commonFormats ++ Seq(TwitterInstantSerializer)
}
