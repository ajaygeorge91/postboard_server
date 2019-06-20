package models

import java.util.UUID

import org.joda.time.DateTime
import play.api.libs.json.Json
import utils.UuidUtils

/**
  * Created by ajayg on 10/2/2016.
  */
case class UserNotification(
                             id: String,
                             content: Option[String] = None,
                             subContent: Option[String] = None,
                             nodeIdOfInterest: Option[String] = None, // nodeOfInterest means the comment/post node that the notification is linked to
                             link: Option[String] = None,
                             imageThumb: Option[String] = None,
                             imageNode: Option[String] = None,
                             read: Option[Boolean] = Some(false),
                             createdAt: Option[String] = None,
                             updatedAt: Option[String] = None
                           )

object UserNotification {
  implicit val jsonFormat = Json.format[UserNotification]

  def getNew(content: String, nodeIdOfInterest: String, subContent:String, imageNode:Option[String] = None) = UserNotification(
    id = UuidUtils.getUUID,
    content = Some(content),
    subContent = Some(subContent),
    imageNode = imageNode,
    nodeIdOfInterest = Some(nodeIdOfInterest),
    createdAt = Some(DateTime.now().toString),
    updatedAt = Some(DateTime.now().toString)
  )

}

