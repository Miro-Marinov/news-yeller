package finrax.clients.twitter.domain.entities

final case class Location(country: String,
                          countryCode: Option[String],
                          name: String,
                          parentid: Long,
                          placeType: PlaceType,
                          url: String,
                          woeid: Long)
