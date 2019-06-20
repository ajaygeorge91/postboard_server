package models

import play.api.libs.json.{Format, Json}

/**
  * Created by ajayg on 12/20/2016.
  *
  * Relationship between a user and the comment/subComment (like/dislike)
  */

case class UserCommentActivity(
                                override val rating: Option[Int], // +1 for a like and -1 for dislike. 0/None for neutral
                                commentID: Option[String] = None
                              ) extends UserActivityParams

object UserCommentActivity {

  implicit val jsonFormat: Format[UserCommentActivity] = Json.format[UserCommentActivity]

  def getNone: UserCommentActivity = {
    UserCommentActivity(rating = None)
  }
}

