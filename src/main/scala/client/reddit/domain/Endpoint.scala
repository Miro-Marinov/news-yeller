package finrax.clients.reddit.domain

object Endpoint {

  val access_token = "https://www.client.reddit.com/api/v1/access_token"
  val me = "https://oauth.client.reddit.com/api/v1/me"

  val more_children = "https://oauth.client.reddit.com/api/morechildren"

  val subscribe = "https://oauth.client.reddit.com/api/subscribe"
  val unsubscribe = "https://oauth.client.reddit.com/api/subscribe"

  val vote = "https://oauth.client.reddit.com/api/vote"
  val save = "https://oauth.client.reddit.com/api/save"
  val unsave = "https://oauth.client.reddit.com/api/unsave"

  val reply = "https://oauth.client.reddit.com/api/comment"
  val delete = "https://oauth.client.reddit.com/api/del"
  val report = "https://oauth.client.reddit.com/api/report"
  val edit = "https://oauth.client.reddit.com/api/editusertext"
  val submit = "https://oauth.client.reddit.com/api/submit"

  val block = "https://oauth.client.reddit.com/api/block"
  val private_message = "https://oauth.client.reddit.com/api/compose"

  val inbox = "https://oauth.client.reddit.com/message/inbox"
  val messages_unread = "https://oauth.client.reddit.com/message/unread"
  val messages_only = "https://oauth.client.reddit.com/message/messages"
  val messages_mentions = "https://oauth.client.reddit.com/message/mentions"
  val messages_post_replies = "https://oauth.client.reddit.com/message/selfreply"
  val messages_comment_replies = "https://oauth.client.reddit.com/message/comments"
  val messages_sent = "https://oauth.client.reddit.com/message/sent"

  val subscribed_subreddits = "https://oauth.client.reddit.com/subreddits/mine/subscriber?limit=1000"


  def saved_posts(name: String): String = "https://oauth.client.reddit.com/user/" + name + "/saved/?type=links"

  def saved_comments(name: String) = "https://oauth.client.reddit.com/user/" + name + "/saved?type=comments"

  def user_posts(name: String) = "https://oauth.client.reddit.com/user/" + name + "/submitted"

  def user_comments(name: String) = "https://oauth.client.reddit.com/user/" + name + "/comments"

  def about_subreddit(name: String) = {
    "https://oauth.client.reddit.com/r/" + name + "/about"
  }

  def subreddit(name: String, sort: Sorting.Value = Sorting.HOT): String =
    s"https://oauth.client.reddit.com/r/$name/$sort"


  def about_user(name: String) = {
    "https://oauth.client.reddit.com/user/" + name + "/about"
  }

  def about_post(name: String) = {
    "https://oauth.client.reddit.com/by_id/t3_" + name
  }

  def post_comment_stream(subreddit: String, post_id: String) = {
    "https://oauth.client.reddit.com/r/" + subreddit + "/comments/" + post_id
  }
}