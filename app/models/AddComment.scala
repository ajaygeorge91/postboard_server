package models

import play.api.libs.json.Json

/**
  * Created by ajayg on 10/2/2016.
  */
case class AddComment(
                       content: String,
                       articleID: String
                     )


object AddComment {
  implicit val jsonFormat = Json.format[AddComment]
}


