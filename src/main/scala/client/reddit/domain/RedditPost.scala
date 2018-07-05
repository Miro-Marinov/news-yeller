package client.reddit.domain

case class RedditPost(
  author : String,
  score : Int,
  permalink: String,
  title : String
)

object RedditPost {
  implicit val ord: Ordering[RedditPost] = Ordering.by({
    post: RedditPost => post.score
  }).reverse
}
