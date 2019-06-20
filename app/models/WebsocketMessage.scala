package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
  * Created by ajayg on 12/28/2016.
  */
case class WebsocketMessage[I](
                               data: I,
                               messageType: String
                             )





case object WebsocketMessage extends {
  implicit def pageFormat[I: Format]: Format[WebsocketMessage[I]] =
    ((__ \ "data").format[I] ~
      (__ \ "messageType").format[String]) (WebsocketMessage.apply, unlift(WebsocketMessage.unapply))


  val STRING = "string"
  val USER_NOTIFICATION = "user_notification"
  val USER_NOTIFICATION_LIST = "user_notification_list"

}