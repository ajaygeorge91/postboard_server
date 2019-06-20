package models

import play.api.libs.json.Json

/**
  * Created by ajayg on 12/20/2016.
  *
  * Relationship between a user and the article (like/comment)
  */

case class UserAction(
                       val relationshipLabel: String,
                       val userActivity: Option[UserActivityImpl] = None
                     )

object UserAction {
  implicit val jsonFormat = Json.format[UserAction]
}
