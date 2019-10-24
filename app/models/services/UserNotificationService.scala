package models.services

import com.google.inject.Inject
import controllers.api.routes
import models.UserNotification
import models.daos.UserNotificationDAO
import play.api.Configuration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by Android SD-1 on 27-03-2017.
  */
class UserNotificationService @Inject()(
                                         userNotificationDAO: UserNotificationDAO,
                                         configuration: Configuration
                                       ) {


  private def getNotificationLinkToClick(userId: String, nodeIdOfInterest: Option[String]): Option[String] = {
    nodeIdOfInterest.map { nii =>
      controllers.routes.UserController.getArticleFromNotificationsID(userId, nii).url
    }
  }

  def addNotification(userID: String, userNotification: UserNotification): Future[UserNotification] = {
    userNotificationDAO.addUserNotification(userID, userNotification).map { f =>
      f.copy(link = getNotificationLinkToClick(userID, userNotification.nodeIdOfInterest))
    }
  }

  def getImageUrlFromImageNodeId(nodeId: String): Future[Option[String]] = {
    userNotificationDAO.getImageUrlFromImageNodeId(nodeId) map { list =>
      list.find(f => f.nonEmpty)
    }
  }

  def getUserNotificationsConnectedToUser(userID: String, pageNumber: Int, numberOfRecords: Int): Future[List[UserNotification]] = {
    userNotificationDAO.getUserNotificationsConnectedToUser(userID, pageNumber, numberOfRecords).map(f => f.map { userNotification =>
      userNotification.copy(link = getNotificationLinkToClick(userID, userNotification.nodeIdOfInterest))
    })
  }

  def readAllNotificationsAndRetrieve(userID: String, pageNumber: Int, numberOfRecords: Int): Future[List[UserNotification]] = {
    userNotificationDAO.readAllNotificationsForUser(userID, pageNumber, numberOfRecords).map(f => f.map { userNotification =>
      userNotification.copy(link = getNotificationLinkToClick(userID, userNotification.nodeIdOfInterest))
    })
  }

  def getNotificationFromNodeOfInterestID(nodeOfInterestID: String): Future[Option[UserNotification]] = {
    userNotificationDAO.getUserNotificationFromNodeOfInterestID(nodeOfInterestID)
  }

}
