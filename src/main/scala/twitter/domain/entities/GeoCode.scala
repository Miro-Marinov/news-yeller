package twitter.domain.entities

final case class GeoCode(latitude: Double, longitude: Double, radius: Accuracy) {
  override def toString = s"$latitude,$longitude,$radius"
}
