package models

import play.api.libs.json.Json

/**
  * Created by ajayg on 10/2/2016.
  */
case class AddSubComment(
                          content: String,
                          commentID: String
                        )


object AddSubComment {
  implicit val jsonFormat = Json.format[AddSubComment]
}



