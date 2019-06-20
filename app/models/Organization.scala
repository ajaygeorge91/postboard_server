package models

import java.util.UUID

import play.api.libs.json.Json

/**
  * Created by ajayg on 10/2/2016.
  */
case class Organization(
                         id: String,
                         name: String
                       )

object Organization {
  implicit val jsonFormat = Json.format[Organization]

}
