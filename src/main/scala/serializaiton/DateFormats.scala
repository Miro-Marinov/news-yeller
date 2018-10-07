package serializaiton

import java.time.{ZoneId, ZoneOffset}
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.util.Locale

object DateFormats {
  // GMTOffsetTimeZone
  val redditDTF: DateTimeFormatter = new DateTimeFormatterBuilder()
    .appendPattern("EEE, d MMM yyyy HH:mm:ss Z") //Thu, 14 Jun 2018 18:23:16 +0000
    .toFormatter()
    .withZone(ZoneId.ofOffset("", ZoneOffset.UTC))
    .withLocale(Locale.ENGLISH)

  // GMTOffsetTimeZone
  val twitterDTF: DateTimeFormatter = new DateTimeFormatterBuilder()
    .appendPattern("EEE MMM d HH:mm:ss Z yyyy") //Thu Jul 05 18:10:32 +0000 2018
    .toFormatter()
    .withZone(ZoneId.ofOffset("", ZoneOffset.UTC))
    .withLocale(Locale.ENGLISH)


  // RFC822TimeZone
  val blockChainBlogRssFeed: DateTimeFormatter = new DateTimeFormatterBuilder()
    .appendPattern("EEE, d MMM yyyy HH:mm:ss z") //Thu, 05 Jul 2018 13:00:00 GMT
    .toFormatter()
    .withZone(ZoneId.ofOffset("", ZoneOffset.UTC))
    .withLocale(Locale.ENGLISH)


  // GMTOffsetTimeZone
  val finraxDTF: DateTimeFormatter = new DateTimeFormatterBuilder()
    .appendPattern("EEE, d MMM yyyy HH:mm:ss Z") //Thu, 14 Jun 2018 18:23:16 +0000
    .toFormatter()
    .withZone(ZoneId.ofOffset("", ZoneOffset.UTC))
    .withLocale(Locale.ENGLISH)
}
