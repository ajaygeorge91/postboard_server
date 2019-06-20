package controllers.api

import javax.inject.{Inject, Named}

import actors.{ReadAllNotification, UserNotificationActor}
import actors.classifications.MessageBus
import akka.actor.ActorRef
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models.{User, UserProfileDTO}
import models.services._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import utils.Constants
import utils.auth.JwtEnv

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserController @Inject()(
                                val messagesApi: MessagesApi,
                                messageBus: MessageBus,
                                articleService: ArticleService,
                                userService: UserService,
                                notificationService: UserNotificationService,
                                userProfileService: UserProfileService,
                                @Named(UserNotificationActor.name) val userNotificationActor: ActorRef,
                                commentService: CommentService,
                                silhouette: Silhouette[JwtEnv])
  extends Controller with I18nSupport {


  def getFromId(userID: String): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    val user = request.identity match {
      case Some(u) if u.id == userID => userService.getProfileFromID(userID)
      case _ => userService.getFromID(userID)
    }
    user map {
      case Some(u) =>
        Ok(Json.obj(
          "success" -> true,
          "data" -> u))
      case None =>
        Ok(Json.obj(
          "success" -> false,
          "message" -> "no user"))
    }
  }


  def getProfileBundleFromUserId(userID: String, pageNumber: Option[Int], numberOfRecords: Option[Int]): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    val user = request.identity match {
      case Some(u) if u.id == userID => userService.getProfileFromID(userID)
      case _ => userService.getFromID(userID)
    }
    user flatMap {
      case Some(u) =>
        articleService.getArticlesFromUserWithUserOpt(userID, request.identity, pageNumber.getOrElse(0), numberOfRecords.getOrElse(Constants.DEFAULT_NO_OF_RECORDS)) map { r =>
          Ok(Json.obj(
            "success" -> true,
            "data" -> UserProfileDTO(userDTO = u, articleList = r)))
        }
      case None => Future.successful(Ok(Json.obj(
        "success" -> false,
        "message" -> "no user")))
    }
  }

  def getUserArticles(userId: String, pageNumber: Option[Int], numberOfRecords: Option[Int]): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    articleService.getArticlesFromUserWithUserOpt(userId, request.identity, pageNumber.getOrElse(0), numberOfRecords.getOrElse(Constants.DEFAULT_NO_OF_RECORDS)) map { r =>
      Ok(Json.obj(
        "success" -> true,
        "data" -> r))
    }
  }

  def getArticlesFromLoggedInUser(pageNumber: Option[Int], numberOfRecords: Option[Int]): Action[AnyContent] = silhouette.SecuredAction.async { implicit request =>
    articleService.getArticlesFromUserWithUserOpt(request.identity.id, Some(request.identity), pageNumber.getOrElse(0), numberOfRecords.getOrElse(Constants.DEFAULT_NO_OF_RECORDS)) map { r =>
      Ok(Json.obj(
        "success" -> true,
        "data" -> r))
    }
  }

  def getUserActivity(pageNumber: Option[Int], numberOfRecords: Option[Int]): Action[AnyContent] = silhouette.SecuredAction.async { implicit request =>
    userProfileService.getActions(request.identity.id, pageNumber.getOrElse(0), numberOfRecords.getOrElse(Constants.DEFAULT_NO_OF_RECORDS)) map { r =>
      Ok(Json.obj(
        "success" -> true,
        "data" -> r))
    }
  }

  def getUserNotifications(pageNumber: Option[Int], numberOfRecords: Option[Int]): Action[AnyContent] = silhouette.SecuredAction.async { implicit request =>
    notificationService.getUserNotificationsConnectedToUser(
      request.identity.id,
      pageNumber.getOrElse(0),
      numberOfRecords.getOrElse(Constants.DEFAULT_NO_OF_RECORDS)) map { r =>
      Ok(Json.obj(
        "success" -> true,
        "data" -> r))
    }
  }

  def notificationClickAction(): Action[AnyContent] = silhouette.SecuredAction { implicit request =>
    userNotificationActor ! ReadAllNotification(request.identity.id)
    Ok(Json.obj(
      "success" -> true,
      "data" -> ""))
  }

}
