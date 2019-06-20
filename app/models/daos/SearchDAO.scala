package models.daos

import models.{Article, User, UserActivity, UserNotification}

import scala.concurrent.Future

/**
  * Created by Android SD-1 on 27-03-2017.
  */
trait SearchDAO {

  def getArticlesFromQuery(userID: String, searchKey: String, pageNumber: Int, numberOfRecords: Int): Future[List[Article]]

  def getUsersFromQuery(userID: String, searchKey: String, pageNumber: Int, numberOfRecords: Int): Future[List[User]]

}
