package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

/**
  * The form which handles the sign up process.
  */
object SignUpForm {

  /**
    * A play framework form.
    */
  val form = Form(
    mapping(
      "fullName" -> nonEmptyText,
      "email" -> email,
      "password" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  /**
    * The form data.
    *
    * @param fullName The first name of a user.
    * @param email    The email of the user.
    * @param password The password of the user.
    */
  case class Data(
                   fullName: String,
                   email: String,
                   password: String)

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
