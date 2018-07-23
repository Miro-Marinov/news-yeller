package finrax.clients.twitter.domain.entities

final case class User(email: Option[String] = None,
                      favourites_count: Int,
                      followers_count: Int,
                      id_str: String,
                      name: String,
                      profile_banner_url: Option[String] = None)
