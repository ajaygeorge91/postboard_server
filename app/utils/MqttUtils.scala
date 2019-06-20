package utils

import java.io.Serializable

import com.sandinh.paho.akka.Message
import models.{UserNotification, WebsocketMessage}
import play.api.libs.json.{JsResult, JsValue, Json}


/**
  * Created by Android SD-1 on 27-03-2017.
  */
object MqttUtils {

  def getNotificationChannelForUser(userId: String): String = {
    userId + "/notification"
  }

  def getNotificationAsByteArray(userNotification:UserNotification):Array[Byte] = {
    val websocketMessage = WebsocketMessage(data = userNotification, messageType = WebsocketMessage.USER_NOTIFICATION)
    val js = Json.toJson(websocketMessage)
    js.toString().getBytes("utf-8")
  }

  def getNotificationListAsByteArray(nl: List[UserNotification]):Array[Byte] = {
    val websocketMessage = WebsocketMessage(data = nl, messageType = WebsocketMessage.USER_NOTIFICATION_LIST)
    val js = Json.toJson(websocketMessage)
    js.toString().getBytes("utf-8")
  }

  def convertMessageToString(message:Message): String = {
    if(message.topic.contains("/notification")){
      val payload = new String(message.payload)
      payload
    }
    else {
      ""
    }
  }

}
