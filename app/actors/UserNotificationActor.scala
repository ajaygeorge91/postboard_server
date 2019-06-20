package actors

import java.util.UUID
import java.util.regex.Pattern

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.google.inject.Inject
import com.sandinh.paho.akka.{MqttPubSub, PSConfig, Publish}
import models.daos.{ArticleDAO, CommentDAO}
import models._
import models.services.{CommentService, UserNotificationService}
import org.joda.time.DateTime
import utils.{Constants, MqttUtils}

import scala.concurrent.ExecutionContext
import scala.util.Random

/**
  * Created by Android SD-1 on 27-03-2017.
  */

case class AddUserCommentNotification(articleID: String, comment: Comment, commentedUser: User)

case class AddUserSubCommentNotification(commentID: String, subComment: SubComment, commentedUser: User)

case class ReadAllNotification(userID: String)

class UserNotificationActor @Inject()(
                                       userNotificationService: UserNotificationService,
                                       articleDAO: ArticleDAO,
                                       commentDAO: CommentDAO
                                     )(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  val pubsub: ActorRef = context.actorOf(Props(classOf[MqttPubSub], PSConfig(
    brokerUrl = "tcp://localhost:1883", //all params is optional except brokerUrl
    userName = null,
    password = null
  )))

  override def receive: Receive = {
    case ReadAllNotification(userID: String) =>
      userNotificationService.readAllNotificationsAndRetrieve(userID, 0, Constants.DEFAULT_NO_OF_RECORDS) map (f => pubsub ! Publish(MqttUtils.getNotificationChannelForUser(userID), MqttUtils.getNotificationListAsByteArray(f)))
    case AddUserCommentNotification(articleID: String, comment: Comment, commentedUser: User) =>
      articleDAO.getArticleUserID(articleID) map {
        case Some(userID) =>
          if (!userID.equalsIgnoreCase(commentedUser.id)) {

            val userNotification = userNotificationService.getNotificationFromNodeOfInterestID(articleID) map {
              case Some(notification) =>
                val imageNode =
                  if (notification.read.getOrElse(false))
                    Some(comment.id)
                  else
                    Some(articleID)
                notification.copy(updatedAt = Some(DateTime.now().toString),
                  content =
                    if (notification.read.getOrElse(false))
                      Some(UserNotificationActor.getNewCommentContent(commentedUser.fullName.getOrElse("<unnamed user>")))
                    else
                      Some(UserNotificationActor.getUpdatedCommentContent(notification.content.getOrElse(""), commentedUser.fullName.getOrElse("<unnamed user>"))),
                  subContent = Some(comment.content.getOrElse("")),
                  read = Some(false),
                  imageNode = imageNode
                )
              case None => // create new
                UserNotification.getNew(
                  content = UserNotificationActor.getNewCommentContent(commentedUser.fullName.getOrElse("<unnamed user>")),
                  nodeIdOfInterest = articleID,
                  subContent = comment.content.getOrElse(""),
                  imageNode = Some(comment.id)
                )
            }
            userNotification map (un => userNotificationService.addNotification(userID, un) map (r => pubsub ! Publish(MqttUtils.getNotificationChannelForUser(userID), MqttUtils.getNotificationAsByteArray(r))))
          }
        case None => log.error("user id not found for article")
      }
    case AddUserSubCommentNotification(commentID: String, subComment: SubComment, commentedUser: User) =>
      commentDAO.getCommentUserID(commentID) map {
        case Some(userID) =>
          if (!userID.equalsIgnoreCase(commentedUser.id)) {
            val userNotification = userNotificationService.getNotificationFromNodeOfInterestID(commentID) map {
              case Some(notification) =>
                notification.copy(updatedAt = Some(DateTime.now().toString),
                  content =
                    if (notification.read.getOrElse(false))
                      Some(UserNotificationActor.getNewSubCommentContent(commentedUser.fullName.getOrElse("<unnamed user>")))
                    else
                      Some(UserNotificationActor.getUpdatedSubCommentContent(notification.content.getOrElse(""), commentedUser.fullName.getOrElse("<unnamed user>"))),
                  subContent = Some(subComment.content.getOrElse("")),
                  imageNode =
                    Some(subComment.id)
                )
              case None => // create new
                UserNotification.getNew(
                  content = UserNotificationActor.getNewSubCommentContent(commentedUser.fullName.getOrElse("<unnamed user>")),
                  nodeIdOfInterest = commentID,
                  subContent = subComment.content.getOrElse(""),
                  imageNode = Some(subComment.id)
                )
            }
            userNotification map (un => userNotificationService.addNotification(userID, un) map (r => pubsub ! Publish(MqttUtils.getNotificationChannelForUser(userID), MqttUtils.getNotificationAsByteArray(r))))
          }
        case None =>
          log.error("user id not found for comment")
      }
  }

}

object UserNotificationActor {

  final val name = "UserNotificationActor"

  def getNewSubCommentContent(commentedUserName: String): String = {
    commentedUserName + " replied to your comment."
  }

  def getUpdatedSubCommentContent(oldContent: String, commentedUserName: String): String = {
    try {
      val m = Pattern.compile("\\d+\\s").matcher(oldContent)
      if (m.find()) {
        val num = m.group().toInt
        (num + 1).toString + " new replies to your comment"
      } else {
        "2 new replies to your comment"
      }
    } catch {
      case ex: Exception =>
        getNewSubCommentContent(commentedUserName)
    }
  }

  def getNewCommentContent(commentedUserName: String): String = {
    commentedUserName + " commented on your post."
  }

  def getUpdatedCommentContent(oldContent: String, commentedUserName: String): String = {
    try {
      val m = Pattern.compile("\\d+\\s").matcher(oldContent)
      if (m.find()) {
        val str = m.group()
        val num = str.trim.toInt
        (num + 1).toString + " new comments on your post"
      } else {
        "2 new comments on your post"
      }
    } catch {
      case ex: Exception =>
        getNewCommentContent(commentedUserName)
    }

  }

}
