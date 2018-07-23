package finrax.serializaiton

import com.esotericsoftware.kryo.Kryo

class KryoInit {
  def customize(kryo: Kryo): Unit = {
    kryo.register(classOf[finrax.actor.aggregator.State], 1313131)
    kryo.register(classOf[finrax.clients.twitter.domain.entities.Tweet], 1313132)
    kryo.register(classOf[client.reddit.domain.RedditPost], 1313133)
    kryo.register(classOf[finrax.client.cryptocontrol.domain.Article], 1313134)
    kryo.register(classOf[java.io.Serializable], 1313135)
    kryo.register(classOf[finrax.client.cryptocontrol.domain.Article], 1313137)
    kryo.register(classOf[finrax.client.cryptocontrol.domain.Coin], 1313138)
    kryo.register(classOf[finrax.client.cryptocontrol.domain.SimilarArticle], 1313139)
    kryo.register(classOf[finrax.clients.rssfeed.RssFeedEntry], 13131311)
    kryo.register(classOf[finrax.clients.twitter.domain.entities.User], 13131310)
  }
}