package models

import play.api.libs.json.Json

/**
  * Created by ajay on 3/19/2016.
  */

case class AuthResultDTO (
                          token:String,
                          userDTO: Option[User] = None
                          )

object AuthResultDTO {

  implicit val jsonFormat = Json.format[AuthResultDTO]
}