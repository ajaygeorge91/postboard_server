package models.daos

import models.{User, Organization, Article}

import scala.concurrent.Future

/**
  * Give access to the article object.
  */
trait ArticleDAO {
  def getNodeType(nodeID: String): Future[List[String]]

  def getArticlesConnectedToUser(userId: String, pageNumber: Int, numberOfRecords: Int): Future[List[Article]]

  def create(article: Article, user: User): Future[Option[Article]]

  def getArticlePublicDetails(articleId: String): Future[Option[Article]]

  def getArticleUserID(articleID: String): Future[Option[String]]

  def getNewArticles(pageNumber: Int, numberOfRecords: Int): Future[List[Article]]

  def getArticleFromLink(link:String, pageNumber: Int, numberOfRecords: Int): Future[List[Article]]

  def getHotArticles(pageNumber: Int, numberOfRecords: Int): Future[List[Article]]
}
