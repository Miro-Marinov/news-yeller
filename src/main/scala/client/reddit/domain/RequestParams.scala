package finrax.clients.reddit.domain

import scala.collection.mutable

case class RequestParams(params: mutable.Map[String, Option[String]] = mutable.Map()) {

  def before(name: String): this.type = {
    params("before") = Some(name)
    this
  }

  def after(name: String): this.type = {
    params("after") = Some(name)
    this
  }

  def limit(num: Int): this.type = {
    params("limit") = Some(num.toString)
    this
  }

  def count(num: Int): this.type = {
    params("count") = Some(num.toString)
    this
  }

  def show(opt: String): this.type = {
    params("show") = Some(opt)
    this
  }

  def time(period: String): this.type = {
    params("t") = Some(period)
    this
  }
}

object RequestParams {
  implicit def toMap(requestParams: RequestParams): Map[String, String] = requestParams.params collect { case (k, Some(v)) => (k, v) } toMap
}
