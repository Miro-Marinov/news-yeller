package twitter

import java.net.URLEncoder

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query

object Implicits {
  implicit class RichString(val value: String) {
    def urlEncoded: String = URLEncoder.encode(value, "UTF-8").replace("+", "%20")
  }


  implicit class RichUri(val uri: Uri) {
    val base: String = uri.withQuery(Query()).toString
  }
}
