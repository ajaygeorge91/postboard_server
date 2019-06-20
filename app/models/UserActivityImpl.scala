package models

import play.api.libs.json.Json

/**
  * Created by ajayg on 12/20/2016.
  *
  * Relationship between a user and the article (like/comment)
  */

case class UserActivityImpl(
                             override val rating: Option[Int] = None
                           ) extends UserActivityParams

object UserActivityImpl {
  implicit val jsonFormat = Json.format[UserActivityImpl]
}
