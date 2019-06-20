package models.services

import com.google.inject.Inject
import models.daos.{ArticleDAO, UserArticleActivityDAO}
import models._
import org.joda.time.DateTime
import utils.{Constants, UuidUtils}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by ajayg on 10/5/2016.
  */
class ArticleServiceImpl @Inject()(articleDAO: ArticleDAO,
                                   userArticleActivityDAO: UserArticleActivityDAO) extends ArticleService {

  override def createArticle(article: Article, user: User): Future[Article] = {
//    val hashTags = List.empty[String]
//    article.articleType match {
//      case Some(atype) if atype.equalsIgnoreCase(Article.ARTICLE_TYPE_POST) =>
//        article.content.getOrElse("")
//      case _ =>
//    }
    articleDAO.create(article.copy(
      createdAt = Some(DateTime.now().toString)), user) flatMap {
      case None => throw new Exception("Error while creating")
      case Some(r) => getArticleDetails(r.id) map {
        case Some(a) => a
        case None => throw new Exception("Error while retrieving")
      }
    }
  }

  def getArticlesForUser(userId: String, pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[Article]] = {
    articleDAO.getArticlesConnectedToUser(userId, pageNumber, numberOfRecords) flatMap { aList =>
      val articleWithUserActivityListF = aList map { a =>
        userArticleActivityDAO.getActivity(userId, a.id) map (act => a.copy(userArticleActivity = act))
      }
      val articleWithDetailList = Future.sequence(articleWithUserActivityListF)
      articleWithDetailList map (r => PaginatedResult.get[Article](pageNumber, r.size, Constants.OrderBy_NewestFirst, r))
    }
  }

  def getArticlesFromUser(otherUserId: String, currentUserId: String, pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[Article]] = {
    articleDAO.getArticlesConnectedToUser(otherUserId, pageNumber, numberOfRecords) flatMap { aList =>
      val articleWithUserActivityListF = aList map { a =>
        userArticleActivityDAO.getActivity(currentUserId, a.id) map (act => a.copy(userArticleActivity = act))
      }
      val articleWithDetailList = Future.sequence(articleWithUserActivityListF)
      articleWithDetailList map (r => PaginatedResult.get[Article](pageNumber, r.size, Constants.OrderBy_NewestFirst, r))
    }
  }

  def getArticlesFromUserWithUserOpt(otherUserId: String, currentUser: Option[User], pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[Article]] = {
    articleDAO.getArticlesConnectedToUser(otherUserId, pageNumber, numberOfRecords) flatMap { aList =>
      val articleWithDetailList = currentUser match {
        case Some(u) =>
          val articleWithUserActivityListF = aList map { a =>
            userArticleActivityDAO.getActivity(u.id, a.id) map (act => a.copy(userArticleActivity = act))
          }
          Future.sequence(articleWithUserActivityListF)
        case None =>
          Future.successful(aList)
      }
      articleWithDetailList map (r => PaginatedResult.get[Article](pageNumber, r.size, Constants.OrderBy_NewestFirst, r))

    }
  }

  override def getArticleDetailsWithUserOpt(user: Option[User], articleID: String): Future[Option[Article]] = {
    articleDAO.getArticlePublicDetails(articleID) flatMap {
      case Some(a) =>
        user match {
          case Some(u) =>
            userArticleActivityDAO.getActivity(u.id, a.id) map { act =>
              Some(a.copy(userArticleActivity = act))
            }
          case None => Future.successful(Some(a))
        }
      case None =>
        Future.successful(None)
    }
  }

  override def getNodeType(nodeID: String): Future[String] = {
    articleDAO.getNodeType(nodeID) map { stringList =>
      stringList.headOption.getOrElse("")
    }
  }

  override def getArticleDetails(articleId: String): Future[Option[Article]] = {
    articleDAO.getArticlePublicDetails(articleId)
  }

  override def getArticlesUserOpt(user: Option[User], pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[Article]] = {
    user match {
      case Some(u) => getArticlesWithUserAction(u.id, pageNumber, numberOfRecords) map { l =>
        PaginatedResult.get[Article](pageNumber, l.size, Constants.OrderBy_NewestFirst, l)
      }
      case None => getArticles(pageNumber, numberOfRecords) map { l =>
        PaginatedResult.get[Article](pageNumber, l.size, Constants.OrderBy_NewestFirst, l)
      }
    }
  }

  override def getHotArticlesUserOpt(user: Option[User], pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[Article]] = {
    user match {
      case Some(u) => getHotArticlesWithUserAction(u.id, pageNumber, numberOfRecords) map { l =>
        PaginatedResult.get[Article](pageNumber, l.size, Constants.OrderBy_HotFirst, l)
      }
      case None => getHotArticles(pageNumber, numberOfRecords) map { l =>
        PaginatedResult.get[Article](pageNumber, l.size, Constants.OrderBy_HotFirst, l)
      }
    }
  }

  override def getArticles(pageNumber: Int, numberOfRecords: Int): Future[List[Article]] = {
    articleDAO.getNewArticles(pageNumber, numberOfRecords) map (l =>
      l.sortWith(_.createdAt.getOrElse(DateTime.now().toString) > _.createdAt.getOrElse(DateTime.now().toString)))
  }

  override def getHotArticles(pageNumber: Int, numberOfRecords: Int): Future[List[Article]] = {
    articleDAO.getHotArticles(pageNumber, numberOfRecords) map (l =>
      l.sortWith(_.articleMeta.getOrElse(ArticleMeta()).rank.getOrElse(0D) > _.articleMeta.getOrElse(ArticleMeta()).rank.getOrElse(0D)))
  }

  override def getArticlesWithUserAction(userId: String, pageNumber: Int, numberOfRecords: Int): Future[List[Article]] = {
    getArticles(pageNumber, numberOfRecords) flatMap { aList =>
      val articleWithUserActivityListF = aList map { a =>
        userArticleActivityDAO.getActivity(userId, a.id) map (act => a.copy(userArticleActivity = act))
      }
      val articleWithDetailList = Future.sequence(articleWithUserActivityListF) map (l =>
        l.sortWith(_.createdAt.getOrElse(DateTime.now().toString) > _.createdAt.getOrElse(DateTime.now().toString)))
      articleWithDetailList
    }
  }

  override def getArticlesFromLink(linkUrl: String, pageNumber: Int, numberOfRecords: Int): Future[List[Article]] = {
    articleDAO.getArticleFromLink(linkUrl, pageNumber, numberOfRecords)
  }

  override def getHotArticlesWithUserAction(userId: String, pageNumber: Int, numberOfRecords: Int): Future[List[Article]] = {
    getHotArticles(pageNumber, numberOfRecords) flatMap { aList =>
      val articleWithUserActivityListF = aList map { a =>
        userArticleActivityDAO.getActivity(userId, a.id) map (act => a.copy(userArticleActivity = act))
      }
      val articleWithDetailList = Future.sequence(articleWithUserActivityListF) map (l =>
        l.sortWith(_.articleMeta.getOrElse(ArticleMeta()).rank.getOrElse(0D) > _.articleMeta.getOrElse(ArticleMeta()).rank.getOrElse(0D)))
      articleWithDetailList
    }
  }

  override def getHotArticleReactionForFirstNPages(user: User, page: Int): Future[List[UserArticleActivity]] = {
    getHotArticles(0, page * Constants.DEFAULT_NO_OF_RECORDS) flatMap { aList =>
      val userActivityListF = aList map { a =>
        userArticleActivityDAO.getActivity(user.id, a.id) map (actO => actO map (act => act.copy(articleID = Some(a.id))))
      }
      val userActivityList = Future.sequence(userActivityListF)
      userActivityList.map(f => f.flatten)
    }
  }

  override def getNewArticleReactionForFirstNPages(user: User, page: Int): Future[List[UserArticleActivity]] = {
    getArticles(0, page * Constants.DEFAULT_NO_OF_RECORDS) flatMap { aList =>
      val userActivityListF = aList map { a =>
        userArticleActivityDAO.getActivity(user.id, a.id) map (actO => actO map (act => act.copy(articleID = Some(a.id))))
      }
      val userActivityList = Future.sequence(userActivityListF)
      userActivityList.map(f => f.flatten)
    }
  }

  override def getArticleActivity(user: User, articleID: String): Future[Option[UserArticleActivity]] = {
    userArticleActivityDAO.getActivity(user.id, articleID)
  }

  override def addArticleActivityFromUser(userId: String, articleId: String, userArticleActivity: UserArticleActivity): Future[UserArticleActivity] = {
    userArticleActivityDAO.addActivity(userId, articleId, userArticleActivity.copy(articleID = None)) map {
      case Some(x) => x
      case None => throw new Exception("error adding articleActivity")
    }
  }

}
