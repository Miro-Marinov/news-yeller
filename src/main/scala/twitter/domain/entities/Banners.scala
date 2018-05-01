package twitter.domain.entities

final case class Banners(sizes: Map[String, Banner] = Map.empty)

final case class Banner(h: Int, w: Int, url: String)
