package finrax.clients.twitter.auth

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.RawHeader
import finrax.clients.twitter.Implicits._
import finrax.clients.twitter.StatusFilter
import javax.inject.Singleton

import scala.util.Random

@Singleton
class OAuthHeaderGenerator() {

  def getAuthHeader(request: HttpRequest,
                    filter: StatusFilter,
                    accessToken: AccessToken,
                    consumerToken: ConsumerToken): RawHeader = {
    val queryParams = request.uri.query().toMap
    val bodyParams = filter.asEncodedMap
    val oauthParams = Map(
      "oauth_consumer_key" -> consumerToken.key,
      "oauth_signature_method" -> "HMAC-SHA1",
      "oauth_version" -> "1.0",
      "oauth_token" -> accessToken.key,
      "oauth_nonce" -> generateNonce,
      "oauth_timestamp" -> generateTimestamp
    )

    val encodedParams = encodeParams(queryParams ++ oauthParams ++ bodyParams).urlEncoded
    val method = request.method.name
    val baseUrl = request.uri.base.urlEncoded

    val mergedParams = s"$method&$baseUrl&$encodedParams"

    val signingKey = Seq(consumerToken.secret.urlEncoded, accessToken.secret.urlEncoded).mkString("&")

    val params = oauthParams + ("oauth_signature" -> Encoder.toHmacSha1(mergedParams, signingKey))
    val authorizationValue = params.mapValues(_.urlEncoded).map { case (k, v) => s"""$k="$v"""" }.toSeq.sorted.mkString(", ")

    params.mapValues(_.urlEncoded)
    RawHeader("Authorization", s"OAuth $authorizationValue")
  }

  private def encodeParams(params: Map[String, String]) =
    params.toSeq.sortBy(_._1).map { case (k, v) => s"$k=$v" }.mkString("&")

  protected def generateTimestamp: String = (System.currentTimeMillis / 1000).toString

  protected def generateNonce: String = Random.alphanumeric.take(42).mkString
}
