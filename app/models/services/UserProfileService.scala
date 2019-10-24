package models.services

import javax.inject.Inject

import models.daos.UserActivityDAO
import models.{UserActivity, PaginatedResult, UserAction}
import utils.Constants
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future


class UserProfileService @Inject()(userActivityDAO: UserActivityDAO) {


  def getActions(userId: String, pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[UserActivity]] = {
    userActivityDAO.getUserActivities(userId, pageNumber, numberOfRecords) map { r =>
      PaginatedResult.get[UserActivity](pageNumber, numberOfRecords, Constants.OrderBy_NewestFirst, r)
    }
  }

}
