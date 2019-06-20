package models.services

import models._

import scala.concurrent.Future

/**
  * Created by ajayg on 10/5/2016.
  */
trait CommentService {

  def createComment(addComment: AddComment, user: User): Future[Comment]

  def editComment(editComment: EditComment, user: User): Future[Comment]

  def createSubComment(addSubComment: AddSubComment, user: User): Future[SubComment]

  def editSubComment(editComment: EditComment, user: User): Future[SubComment]

  def getComment(commentID: String, withUserID: String): Future[Option[Comment]]

  def getCommentWithSubComments(commentID: String, user: Option[User]): Future[Option[Comment]]

  def getHotCommentsForArticle(articleID: String, user: Option[User], pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[Comment]]

  def getHotSubCommentsForComment(commentID: String, user: Option[User], pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[SubComment]]

  def getNewSubCommentsForComment(commentID: String, user: Option[User], pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[SubComment]]

  def getNewCommentsForArticle(articleID: String, user: Option[User], pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[Comment]]

  def addCommentActivityFromUser(userID: String, commentID: String, data: UserCommentActivity): Future[UserCommentActivity]


}
