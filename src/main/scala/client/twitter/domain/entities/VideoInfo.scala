package finrax.clients.twitter.domain.entities

final case class VideoInfo(aspect_ratio: Seq[Int], duration_millis: Option[Long], variants: Seq[Variant])
