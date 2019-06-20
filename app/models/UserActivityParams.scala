package models

import play.api.libs.json.Json

/**
  * Created by ajayg on 12/20/2016.
  *
  * Relationship between a user and the article (like/comment)
  */

trait UserActivityParams {
  val rating: Option[Int] = None
}
