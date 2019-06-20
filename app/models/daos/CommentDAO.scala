package models.daos

import models.{SubComment, Comment, Article, User}

import scala.concurrent.Future

/**
  * Give access to the article object.
  */
trait CommentDAO {

  def createComment(articleID: String, comment: Comment, user: User): Future[Option[Comment]]

  def editCommentContent(commentID: String, content: String): Future[Option[Comment]]

  def editSubCommentContent(subCommentID: String, content: String): Future[Option[SubComment]]

  def createSubComment(commentID: String, subComment: SubComment, user: User): Future[Option[SubComment]]

  def getAllCommentsForArticle(articleID: String, pageNumber: Int, numberOfRecords: Int): Future[List[Comment]]

  def getCommentsDetails(commentID: String, withUserID: String): Future[Option[Comment]]

  def getCommentUserID(commentID: String): Future[Option[String]]

  def getSubCommentsDetails(subCommentID: String): Future[Option[SubComment]]

  def getHotCommentsForArticleWithUserReaction(articleID: String, withUserID: String, pageNumber: Int, numberOfRecords: Int): Future[List[Comment]]

  def getNewCommentsForArticleWithUserReaction(articleID: String, withUserID: String, pageNumber: Int, numberOfRecords: Int): Future[List[Comment]]

  def getHotSubCommentsForCommentWithUserReaction(commentID: String, withUserID: String, pageNumber: Int, numberOfRecords: Int): Future[List[SubComment]]

  def getNewSubCommentsForCommentWithUserReaction(commentID: String, withUserID: String, pageNumber: Int, numberOfRecords: Int): Future[List[SubComment]]
}
