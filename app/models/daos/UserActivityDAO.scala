package models.daos

import models.{UserActivity, UserCommentActivity}

import scala.concurrent.Future

/**
  * Created by ajayg on 12/20/2016.
  */
trait UserActivityDAO {

  def getUserActivities(userID: String, pageNumber: Int, numberOfRecords: Int): Future[List[UserActivity]]

}
