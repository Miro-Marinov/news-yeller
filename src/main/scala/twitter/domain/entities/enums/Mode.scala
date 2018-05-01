package twitter.domain.entities.enums

object Mode extends Enumeration {
  type Mode = Value

  val Private = Value("private")
  val Public = Value("public")
}
