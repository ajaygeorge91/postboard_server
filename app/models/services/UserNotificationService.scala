package models.services

import models._

import scala.concurrent.Future

/**
  * Created by ajayg on 27/3/2017.
  */
trait UserNotificationService {

  def addNotification(userID: String, userNotification: UserNotification): Future[UserNotification]

  def getUserNotificationsConnectedToUser(userID: String, pageNumber: Int, numberOfRecords: Int): Future[List[UserNotification]]

  def getImageUrlFromImageNodeId(nodeId: String): Future[Option[String]]

  def readAllNotificationsAndRetrieve(userID: String, pageNumber: Int, numberOfRecords: Int): Future[List[UserNotification]]

  def getNotificationFromNodeOfInterestID(articleID: String): Future[Option[UserNotification]]

}
