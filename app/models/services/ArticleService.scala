package models.services

import models._

import scala.concurrent.Future

/**
  * Created by ajayg on 10/5/2016.
  */
trait ArticleService {

  def getHotArticleReactionForFirstNPages(user: User, page: Int): Future[List[UserArticleActivity]]

  def getNewArticleReactionForFirstNPages(user: User, page: Int): Future[List[UserArticleActivity]]

  def getNodeType(nodeID: String): Future[String]

  def getArticleDetails(articleId: String): Future[Option[Article]]

  def getArticleDetailsWithUserOpt(user: Option[User], articleId: String): Future[Option[Article]]

  def createArticle(article: Article, user: User): Future[Article]

  def getArticlesForUser(userId: String, pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[Article]]

  def getArticlesFromUserWithUserOpt(otherUserId: String, currentUser: Option[User], pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[Article]]

  def getArticlesFromUser(otherUserId: String, currentUserId: String, pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[Article]]

  def getArticlesUserOpt(user: Option[User], pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[Article]]

  def getHotArticlesUserOpt(user: Option[User], pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[Article]]

  def getArticles(pageNumber: Int, numberOfRecords: Int): Future[List[Article]]

  def getHotArticles(pageNumber: Int, numberOfRecords: Int): Future[List[Article]]

  def getArticlesWithUserAction(userId: String, pageNumber: Int, numberOfRecords: Int): Future[List[Article]]

  def getHotArticlesWithUserAction(userId: String, pageNumber: Int, numberOfRecords: Int): Future[List[Article]]

  def getArticlesFromLink(linkUrl: String, pageNumber: Int, numberOfRecords: Int): Future[List[Article]]

  def getArticleActivity(user: User, articleID: String): Future[Option[UserArticleActivity]]

  def addArticleActivityFromUser(userId: String, articleId: String, userArticleActivity: UserArticleActivity): Future[UserArticleActivity]
}
