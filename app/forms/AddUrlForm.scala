package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

/**
  * The form which handles the submission of the credentials.
  */
object AddUrlForm {

  /**
    * A play framework form.
    */
  val form = Form(
    mapping(
      "name" -> nonEmptyText,
      "url" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  case class Data(
                   name: String,
                   url: String)


  /**
    * The companion object.
    */
  object Data {

    /**
      * Converts the [Date] object to Json and vice versa.
      */
    implicit val jsonFormat = Json.format[Data]
  }


}
