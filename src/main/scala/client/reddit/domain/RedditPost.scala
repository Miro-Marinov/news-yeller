package client.reddit.domain

case class RedditPost(author: String,
                      score: Int,
                      permalink: String,
                      subreddit: String,
                      title: String,
                      stickied: Boolean)

object RedditPost {
  implicit val ord: Ordering[RedditPost] = Ordering.by({
    post: RedditPost => post.score
  })
}