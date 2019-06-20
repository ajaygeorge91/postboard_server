package models.daos

import models.UserNotification

import scala.concurrent.Future

/**
  * Created by Android SD-1 on 27-03-2017.
  */
trait UserNotificationDAO {

  def addUserNotification(userID: String, userNotification: UserNotification): Future[UserNotification]

  def getImageUrlFromImageNodeId(nodeId: String): Future[List[String]]

  def getUserNotificationFromNodeOfInterestID(nodeOfInterestID: String): Future[Option[UserNotification]]

  def getUserNotificationsConnectedToUser(userID: String, pageNumber: Int, numberOfRecords: Int): Future[List[UserNotification]]

  def readAllNotificationsForUser(userID: String, pageNumber: Int, numberOfRecords: Int): Future[List[UserNotification]]
}
