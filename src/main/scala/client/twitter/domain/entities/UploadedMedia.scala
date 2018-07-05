package finrax.clients.twitter.domain.entities

final case class UploadedMedia(media_id: Long,
                               media_id_str: String,
                               size: Int,
                               image: Option[Image] = None,
                               video: Option[Video] = None)
