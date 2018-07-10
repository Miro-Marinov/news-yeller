package finrax.client.cryptocontrol.domain

import io.cryptocontrol.cryptonewsapi.models.{Article => CArticle}

import scala.collection.JavaConverters._

case class Article(id: String,
                   publishedAt: String,
                   hotness: Double,
                   activityHotness: Double,
                   primaryCategory: String,
                   description: String,
                   title: String,
                   url: String,
                   originalImageUrl: String,
                   words: Int,
                   thumbnail: String,
                   coins: List[Coin],
                   similarArticles: List[SimilarArticle])

object Article {

  implicit def convertCryptoControlArticle(article: CArticle): Article = {
    val coins = article.getCoins.asScala.map(c => Coin(id = c.getId, name = c.getName, slug = c.getSlug, tradingSymbol = c.getTradingSymbol)).toList
    val similarArticles = article.getSimilarArticles.asScala.map(a => SimilarArticle(id = a.getId, publishedAt = a.getPublishedAt, title = a.getTitle, url = a.getUrl)).toList
    Article(article.getId,
      article.getPublishedAt,
      article.getHotness.doubleValue(),
      article.getActivityHotness.doubleValue(),
      article.getPrimaryCategory,
      article.getDescription,
      article.getTitle,
      article.getUrl,
      article.getOriginalImageUrl,
      article.getWords.intValue(),
      article.getThumbnail,
      coins,
      similarArticles)
  }

  implicit val ord: Ordering[Article] = Ordering.by({
    article: Article => article.hotness
  })
}