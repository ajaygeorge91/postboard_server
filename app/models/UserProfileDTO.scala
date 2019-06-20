package models

import play.api.libs.json.Json

/**
  * Created by ajay on 3/19/2016.
  */

case class UserProfileDTO(
                           userDTO: User,
                           articleList: PaginatedResult[Article]
                         )


object UserProfileDTO {

  implicit val jsonFormat = Json.format[UserProfileDTO]
}