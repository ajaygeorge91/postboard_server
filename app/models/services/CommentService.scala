package models.services

import java.util.UUID
import javax.inject.Named

import actors.{AddUserCommentNotification, AddUserSubCommentNotification, UserNotificationActor}
import actors.bots.BotSubCommentActor
import akka.actor.ActorRef
import com.google.inject.Inject
import models.daos.{ArticleDAO, CommentDAO, UserArticleActivityDAO, UserCommentActivityDAO}
import models._
import org.joda.time.DateTime
import utils.{CommentUtils, Constants, StringUtils}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by ajayg on 10/5/2016.
  */
class CommentService @Inject()(commentDAO: CommentDAO,
                               userCommentActivityDAO: UserCommentActivityDAO,
                               @Named(UserNotificationActor.name) val userNotificationActor: ActorRef
                              ) {

  def createComment(addComment: AddComment, user: User): Future[Comment] = {
    val c = Comment.get(addComment.content)
    commentDAO.createComment(addComment.articleID, c.copy(createdAt = Some(DateTime.now().toString)), user) map {
      case None => throw new Exception("Error while creating")
      case Some(com) =>
        userNotificationActor ! AddUserCommentNotification(addComment.articleID, com, user)
        com
    }
  }

  def editComment(editComment: EditComment, user: User): Future[Comment] = {
    commentDAO.getCommentsDetails(editComment.commentID, "") flatMap {
      case None => throw new Exception("No original comment found")
      case Some(c) => if (c.createdBy.getOrElse(User(id = "")).id.equals(user.id)) {
        commentDAO.editCommentContent(editComment.commentID, StringUtils.getTrimmedContent(editComment.content)) map {
          case None => throw new Exception("No original comment found")
          case Some(editedComment) => editedComment
        }
      } else {
        throw new Exception("Different user")
      }
    }
  }

  def getComment(commentID: String, withUserID: String): Future[Option[Comment]] = {
    commentDAO.getCommentsDetails(commentID, withUserID)
  }

  def getCommentWithSubComments(commentID: String, user: Option[User]): Future[Option[Comment]] = {
    commentDAO.getCommentsDetails(commentID, user.getOrElse(User("")).id) flatMap {
      case Some(c) =>
        getHotSubCommentsForComment(c.id, user, 0, 3) map (pSc => Some(c.copy(subCommentListP = Some(pSc))))
      case None =>
        Future.successful(None)
    }
  }

  def editSubComment(editComment: EditComment, user: User): Future[SubComment] = {
    commentDAO.getSubCommentsDetails(editComment.commentID) flatMap {
      case None => throw new Exception("No original comment found")
      case Some(c) => if (c.createdBy.getOrElse(User(id = "")).id.equals(user.id)) {
        commentDAO.editSubCommentContent(editComment.commentID, StringUtils.getTrimmedContent(editComment.content)) map {
          case None => throw new Exception("No original comment found")
          case Some(editedComment) => editedComment
        }
      } else {
        throw new Exception("Different user")
      }
    }
  }

  def createSubComment(addSubComment: AddSubComment, user: User): Future[SubComment] = {
    val sc = SubComment.get(addSubComment.content)
    commentDAO.createSubComment(addSubComment.commentID, sc.copy(createdAt = Some(DateTime.now().toString)), user) map {
      case None => throw new Exception("Error while creating")
      case Some(o) =>
        userNotificationActor ! AddUserSubCommentNotification(addSubComment.commentID, o, user)
        o
    }
  }

  def addCommentActivityFromUser(userID: String, commentID: String, data: UserCommentActivity): Future[UserCommentActivity] = {
    userCommentActivityDAO.addCommentOrSubCommentReaction(userID, commentID, data) map {
      case Some(x) => x
      case None => throw new Exception("error adding commentActivity")
    }
  }

  def getHotCommentsForArticle(articleID: String, user: Option[User], pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[Comment]] = {
    commentDAO.getHotCommentsForArticleWithUserReaction(articleID, user.map(u => u.id).getOrElse(""), pageNumber, numberOfRecords) flatMap { l =>
      val listWithSc = l.map { c =>
        getHotSubCommentsForComment(c.id, user, 0, 3) map (pSc => c.copy(subCommentListP = Some(pSc)))
      }
      Future.sequence(listWithSc).map { l =>
        PaginatedResult.get(pageNumber, l.size, Constants.OrderBy_HotFirst, CommentUtils.paginateSubComments(l, Constants.OrderBy_HotFirst))
      }
    }
  }

  def getNewCommentsForArticle(articleID: String, user: Option[User], pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[Comment]] = {
    commentDAO.getNewCommentsForArticleWithUserReaction(articleID, user.map(u => u.id).getOrElse(""), pageNumber, numberOfRecords) flatMap { l =>
      val listWithSc = l.map { c =>
        getNewSubCommentsForComment(c.id, user, 0, 3) map (pSc => c.copy(subCommentListP = Some(pSc)))
      }
      Future.sequence(listWithSc).map { l =>
        l.sortWith(_.createdAt.getOrElse(DateTime.now().toString) < _.createdAt.getOrElse(DateTime.now().toString))
        PaginatedResult.get(pageNumber, l.size, Constants.OrderBy_NewestFirst, CommentUtils.paginateSubComments(l, Constants.OrderBy_NewestFirst))
      }
    }
  }

  def getHotSubCommentsForComment(commentID: String, user: Option[User], pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[SubComment]] = {
    commentDAO.getHotSubCommentsForCommentWithUserReaction(commentID, user.map(u => u.id).getOrElse(""), pageNumber, numberOfRecords) map { l =>
      PaginatedResult.get(pageNumber, l.size, Constants.OrderBy_HotFirst, l)
    }
  }

  def getNewSubCommentsForComment(commentID: String, user: Option[User], pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[SubComment]] = {
    commentDAO.getNewSubCommentsForCommentWithUserReaction(commentID, user.map(u => u.id).getOrElse(""), pageNumber, numberOfRecords) map { l =>
      PaginatedResult.get(pageNumber, l.size, Constants.OrderBy_HotFirst, l)
    }
  }

}
