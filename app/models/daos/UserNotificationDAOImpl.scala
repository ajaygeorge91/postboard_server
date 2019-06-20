package models.daos

import javax.inject.Inject

import models._
import play.api.{Configuration, Logger}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws._
import utils.Constants

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class UserNotificationDAOImpl @Inject()(neo: Neo4J,
                                        configuration: Configuration) extends UserNotificationDAO {

  override def addUserNotification(userID: String, userNotification: UserNotification): Future[UserNotification] = {
    val jsonUserNotification = Json.toJson(userNotification).as[JsObject]
    neo.cypher(
      """
        | MATCH (u:User{id:{userId}})
        | MERGE (u)-[:HAS_NOTIFICATION]->(n:UserNotification{nodeIdOfInterest:{nodeIdOfInterest}})
        | ON CREATE SET n.timestamp = timestamp()
        | SET n = {props}
        | RETURN n
      """.stripMargin,
      Json.obj(
        "props" -> jsonUserNotification,
        "nodeIdOfInterest" -> userNotification.nodeIdOfInterest.getOrElse("").toString,
        "userId" -> userID
      )).map(parseNeoUserNotification) map {
      case Some(u) => u
      case None => throw new Exception("error creating userNotification")
    }
  }

  override def getImageUrlFromImageNodeId(nodeId: String): Future[List[String]] = {
    neo.cypher(
      """
        |MATCH (a:Article{id:{nodeId}})
        |RETURN a.imageString as img
        |Union ALL MATCH (c:Comment{id:{nodeId}})--(u:User)
        |RETURN u.avatarURL as img
      """.stripMargin, Json.obj(
        "nodeId" -> nodeId
      )).map(r => parseStringList(r))
  }

  override def getUserNotificationFromNodeOfInterestID(nodeIdOfInterest: String): Future[Option[UserNotification]] = {
    neo.cypher(
      """MATCH (n:UserNotification)
        | WHERE n.nodeIdOfInterest = {nodeIdOfInterest}
        | optional MATCH (a{id:n.imageNode})-[:HAS_ARTICLE|:COMMENTED_BY]-(u:User)
        | where a:Article or a:Comment or a:SubComment
        |RETURN n, a.imageString as imageString, u.avatarURL as avatarURL
      """.stripMargin, Json.obj(
        "nodeIdOfInterest" -> nodeIdOfInterest
      )).map(r => parseNeoUserNotification(r))
  }

  override def getUserNotificationsConnectedToUser(userID: String, pageNumber: Int, numberOfRecords: Int): Future[List[UserNotification]] = {
    neo.cypher(
      """
        |MATCH (u1:User{id:{userID}})-[r_n:HAS_NOTIFICATION]->(un)
        | optional MATCH (a{id:un.imageNode})-[:HAS_ARTICLE|:COMMENTED_BY]-(u:User)
        | where a:Article or a:Comment or a:SubComment
        | RETURN un, a.imageString as imageString, u.avatarURL as avatarURL
        | ORDER BY (un.updatedAt) DESC
        | SKIP {skipCount}
        | LIMIT {takeCount}
      """.stripMargin, Json.obj(
        "userID" -> userID,
        "skipCount" -> pageNumber * numberOfRecords,
        "takeCount" -> numberOfRecords
      )).map(r => parseNeoUserNotificationList(r))
  }

  override def readAllNotificationsForUser(userID: String, pageNumber: Int, numberOfRecords: Int): Future[List[UserNotification]] = {
    neo.cypher(
      """
        |MATCH (u1:User{id:{userID}})-[r_n:HAS_NOTIFICATION]->(un)
        | optional MATCH (a{id:un.imageNode})-[:HAS_ARTICLE|:COMMENTED_BY]-(u:User)
        | where a:Article or a:Comment or a:SubComment
        | SET un.read = true
        | RETURN un, a.imageString as imageString, u.avatarURL as avatarURL
        | ORDER BY (un.updatedAt) DESC
        | SKIP {skipCount}
        | LIMIT {takeCount}
      """.stripMargin, Json.obj(
        "userID" -> userID,
        "skipCount" -> pageNumber * numberOfRecords,
        "takeCount" -> numberOfRecords
      )).map(r => parseNeoUserNotificationList(r))
  }

  def parseNeoUserNotificationList(response: WSResponse): List[UserNotification] = {
    Logger.logger.debug(response.toString)
    val jsObj = ((Json.parse(response.body) \ "results") (0) \ "data").as[List[JsObject]]
    val list = jsObj map { ob =>
      val a = (ob \ "row") (0).as[UserNotification]
      val imageString = (ob \ "row") (1).asOpt[String]
      val avatarURL = (ob \ "row") (2).asOpt[String]
      val img = if (imageString.getOrElse("").nonEmpty) {
        Image.getFromString(imageString = imageString.getOrElse(""), config = configuration).getOrElse(Image()).imageURLThumbnail
      } else {
        Some(avatarURL.getOrElse(Constants.DEFAULT_AVATAR))
      }
      a.copy(imageThumb = img)
    }
    list
  }

  def parseStringList(response: WSResponse): List[String] = {
    Logger.logger.debug(response.toString)
    val jsObj = ((Json.parse(response.body) \ "results") (0) \ "data").as[List[JsObject]]
    val list = jsObj map { ob =>
      val a = (ob \ "row") (0).as[String]
      a
    }
    list
  }

  def parseNeoUserNotification(response: WSResponse): Option[UserNotification] = {
    (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (0).asOpt[UserNotification].map { un =>
      val imageString = (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (1).asOpt[String]
      val avatarURL = (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (2).asOpt[String]
      val img = if (imageString.getOrElse("").nonEmpty) {
        Image.getFromString(imageString = imageString.getOrElse(""), config = configuration).getOrElse(Image()).imageURLThumbnail
      } else {
        avatarURL
      }
      un.copy(imageThumb = img)
    }
  }

}