package models.services

import models._

import scala.concurrent.Future

/**
  * Created by ajayg on 10/5/2016.
  */
trait SearchService {

  def getSearchBundleFromKey(user: Option[User], searchKey: String): Future[List[String]]

  def getArticlesFromQuery(user: Option[User], searchKey: String, pageNumber: Int, numberOfRecords: Int): Future[List[Article]]

  def getUsersFromQuery(user: Option[User], searchKey: String, pageNumber: Int, numberOfRecords: Int): Future[List[User]]

}
