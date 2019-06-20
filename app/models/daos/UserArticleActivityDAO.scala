package models.daos

import models.UserArticleActivity

import scala.concurrent.Future

/**
  * Created by ajayg on 12/20/2016.
  */
trait UserArticleActivityDAO {
  def getActivity(userID: String, articleID: String): Future[Option[UserArticleActivity]]

  def addActivity(userID: String, articleID: String, userArticleActivity: UserArticleActivity): Future[Option[UserArticleActivity]]
}
