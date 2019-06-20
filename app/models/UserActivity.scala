package models

import play.api.libs.json.Json

/**
  * Created by ajayg on 12/20/2016.
  *
  * Relationship between a user and the article (like/comment)
  */


case class UserActivity(
                         nodeLabel: String,
                         article: Option[Article] = None,
                         comment: Option[Comment] = None,
                         userActivityList: List[UserAction] = List.empty[UserAction]
                       )


object UserActivity {
  implicit val jsonFormat = Json.format[UserActivity]
}
