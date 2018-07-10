package finrax.clients.twitter

import finrax.clients.twitter.enums.FilterLevel.FilterLevel
import finrax.clients.twitter.enums.Language.Language
import finrax.util.Implicits._

case class StatusFilter(follow: Seq[Long] = Seq.empty,
                        tracks: Seq[String] = Seq.empty,
                        locations: Seq[Double] = Seq.empty,
                        languages: Seq[Language] = Seq.empty,
                        stallWarnings: Option[Boolean] = None,
                        filterLevel: Option[FilterLevel] = None) {


  require(follow.nonEmpty || tracks.nonEmpty || locations.nonEmpty,
    "At least one of 'follow', 'tracks' or 'locations' needs to be non empty")

  val asMap: Map[String, String] = Map(
    "follow" -> follow,
    "track" -> tracks,
    "locations" -> locations,
    "languages" -> languages,
    "stall_warnings" -> stallWarnings,
    "filter_level" -> filterLevel)
    .collect {
      case (k, Some(v)) => (k, v.toString)
      case (k, v: Seq[Any]) if v.nonEmpty => (k, v.mkString(","))
    }

  val asEncodedMap: Map[String, String] = asMap.mapValues(_.urlEncoded)

  val asUrlEncodedParams: String = asEncodedMap
    .map { case (k, v) => s"$k=$v" }
    .toSeq
    .sorted
    .mkString("&")
}
