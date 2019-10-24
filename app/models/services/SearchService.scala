package models.services

import com.google.inject.Inject
import models.{Article, User, UserNotification}
import models.daos.{SearchDAO, UserNotificationDAO}

import scala.concurrent.Future

/**
  * Created by Android SD-1 on 27-03-2017.
  */
class SearchService @Inject()(
                               searchDAO: SearchDAO
                             ) {

  def getArticlesFromQuery(user: Option[User], searchKey: String, pageNumber: Int, numberOfRecords: Int): Future[List[Article]] = {
    searchDAO.getArticlesFromQuery(userID = user.getOrElse(User(id = "")).id, searchKey, pageNumber, numberOfRecords)
  }

  def getUsersFromQuery(user: Option[User], searchKey: String, pageNumber: Int, numberOfRecords: Int): Future[List[User]] = {
    searchDAO.getUsersFromQuery(userID = user.getOrElse(User(id = "")).id, searchKey, pageNumber, numberOfRecords)
  }

  def getSearchBundleFromKey(user: Option[User], searchKey: String): Future[List[String]] = {
    Future.successful(List.empty)
  }
}
