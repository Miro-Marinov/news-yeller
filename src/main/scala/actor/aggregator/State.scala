package finrax.actor.aggregator

import client.reddit.domain.RedditPost
import finrax.client.cryptocontrol.domain.Article
import finrax.clients.rssfeed.RssFeedEntry
import finrax.clients.twitter.domain.entities.Tweet

case class State(tweets: Vector[Tweet] = Vector.empty[Tweet],
                   redditPosts: Vector[RedditPost] = Vector.empty[RedditPost],
                   rssFeedNews: Vector[RssFeedEntry] = Vector.empty[RssFeedEntry],
                   cryptocontrolNews: Vector[Article] = Vector.empty[Article])