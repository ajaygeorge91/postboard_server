package models

import play.api.libs.json.Json

/**
  * Created by ajayg on 12/20/2016.
  *
  * Relationship between a user and the article (like/comment)
  */

case class UserArticleActivity(
                                override val rating: Option[Int], // +1 for a like and -1 for dislike. 0/None for neutral
                                articleID: Option[String] = None
                              ) extends UserActivityParams

object UserArticleActivity {
  implicit val jsonFormat = Json.format[UserArticleActivity]

  def getNone: UserArticleActivity = {
    UserArticleActivity(rating = None)
  }
}

