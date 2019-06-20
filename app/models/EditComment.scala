package models

import play.api.libs.json.Json

/**
  * Created by ajayg on 10/2/2016.
  */
case class EditComment(
                        content: String,
                        commentID: String
                      )


object EditComment {
  implicit val jsonFormat = Json.format[EditComment]
}


