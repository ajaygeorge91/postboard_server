package controllers

import javax.inject.Inject

import actors.classifications.MessageBus
import com.mohiva.play.silhouette.api._
import models.User
import models.services._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import utils.{Constants, StringUtils}
import utils.auth.CookieEnv

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserController @Inject()(
                                val messagesApi: MessagesApi,
                                messageBus: MessageBus,
                                articleService: ArticleService,
                                userService: UserService,
                                notificationService: UserNotificationService,
                                userProfileService: UserProfileService,
                                commentService: CommentService,
                                silhouette: Silhouette[CookieEnv])
  extends Controller with I18nSupport {

  def profile(userID: String): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    val user = request.identity match {
      case Some(u) if u.id == userID => userService.getProfileFromID(userID)
      case _ => userService.getFromID(userID)
    }
    user flatMap {
      case Some(u) =>
        articleService.getArticlesFromUserWithUserOpt(userID, request.identity, 0, Constants.DEFAULT_NO_OF_RECORDS) map { r =>
          Ok(views.html.profile.userProfile(u.fullName.getOrElse("Profile") + "  | PostBoard", request.identity, u, r))
        }
      case None => Future.successful(Ok("Not found"))
    }
  }

  def activity(userID: String): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    if (request.identity.getOrElse(User("")).id.equalsIgnoreCase(userID)) {
      userService.getFromID(userID) flatMap {
        case Some(u) =>
          userProfileService.getActions(userID, 0, 100) map { r =>
            Ok(views.html.profile.userActivity(u.fullName.getOrElse("User") + " activities | PostBoard", request.identity, u, r))
          }
        case None => Future.successful(Ok("Not found"))
      }
    } else {
      Future.successful(Ok("Unauthorized"))
    }
  }

  def notifications(userID: String): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    if (request.identity.getOrElse(User("")).id.equalsIgnoreCase(userID)) {
      userService.getFromID(userID) flatMap {
        case Some(u) =>
          for {
            nl <- notificationService.getUserNotificationsConnectedToUser(userID, 0, Constants.DEFAULT_NO_OF_RECORDS)
          } yield {
            Ok(views.html.profile.userNotifications(u.fullName.getOrElse("User") + " notifications | PostBoard", request.identity, nl, u))
          }
        case None => Future.successful(Ok("Not found"))
      }
    } else {
      Future.successful(Ok("Unauthorized"))
    }
  }


  def getArticleFromNotificationsID(userID: String, nodeID: String): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    if (request.identity.getOrElse(User("")).id.equalsIgnoreCase(userID)) {
      articleService.getNodeType(nodeID) flatMap  {
        case "Article" =>
          Future.successful(Redirect(routes.ArticleController.details(nodeID, None)))
        case "Comment" =>
          commentService.getComment(nodeID, "") map {
            case Some(c) =>
              Redirect(routes.ArticleController.details(c.articleID.getOrElse(""), Some(nodeID)))
            case None => Ok("Not found")
          }
        case _ =>
          Future.successful(Ok("Not found"))
      }
    } else {
      Future.successful(Ok("Unauthorized"))
    }
  }

  def settings(userID: String) = silhouette.UserAwareAction.async { implicit request =>
    userService.getFromID(userID) map {
      case Some(u) =>
        Ok(views.html.profile.profileSetings(u.fullName.getOrElse("User") + "settings | PostBoard", request.identity, u))
      case None => Ok("Not found")
    }
  }

  //
  //  override protected def onNotAuthenticated(request: RequestHeader): Option[Future[Result]] = {
  //    Some(Future.successful(Redirect(routes.ApplicationController.signIn())))
  //  }


}
