package controllers

import javax.inject.{Inject, Named}

import actors.{ReadAllNotification, UserNotificationActor}
import actors.classifications.MessageBus
import akka.actor.{ActorRef, Props}
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.sandinh.paho.akka.{MqttPubSub, PSConfig}
import models._
import models.services.{ArticleService, CommentService, UserNotificationService, UserService}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.{Action, AnyContent, Controller}
import utils.Constants
import utils.auth.CookieEnv

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AjaxController @Inject()(
                                val messagesApi: MessagesApi,
                                messageBus: MessageBus,
                                articleService: ArticleService,
                                commentService: CommentService,
                                userService: UserService,
                                wSClient: WSClient,
                                userNotificationService: UserNotificationService,
                                @Named(UserNotificationActor.name) val userNotificationActor: ActorRef,
                                silhouette: Silhouette[CookieEnv])
  extends Controller with I18nSupport {


  def getHotArticles(pageNumber: Int): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    articleService.getHotArticlesUserOpt(request.identity, pageNumber, Constants.DEFAULT_NO_OF_RECORDS) map { r =>
      if (r.result.isEmpty) Ok("")
      else Ok(views.html.articles._articleList(request.identity, r))
    }
  }

  def getNewArticles(pageNumber: Int): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    articleService.getArticlesUserOpt(request.identity, pageNumber, Constants.DEFAULT_NO_OF_RECORDS) map { r =>
      if (r.result.isEmpty) Ok("")
      else Ok(views.html.articles._articleList(request.identity, r))
    }
  }

  def getNewComments(articleID: String, pageNumber: Int): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    commentService.getNewCommentsForArticle(articleID, request.identity, pageNumber, Constants.DEFAULT_NO_OF_RECORDS) map { r =>
      Ok(Json.obj(
        "success" -> true,
        "data" -> r))
    }
  }

  def getHotComments(articleID: String, pageNumber: Int): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    commentService.getHotCommentsForArticle(articleID, request.identity, pageNumber, Constants.DEFAULT_NO_OF_RECORDS) map { r =>
      //      if (r.result.isEmpty) Ok(views.html.articles._articleNoMoreContents(request.identity, r))
      if (r.result.isEmpty) Ok("")
      else Ok(views.html.articles._commentList(request.identity, r))
    }
  }


  def addArticleReaction(): Action[JsValue] = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[UserArticleActivity].map { data =>
      articleService.addArticleActivityFromUser(request.identity.id.toString, data.articleID.get, data) map { r =>
        Ok(Json.obj(
          "success" -> true,
          "data" -> r))
      }
    }.recoverTotal {
      error =>
        Future.successful(BadRequest(Json.obj(
          "success" -> false,
          "message" -> JsError.toJson(error))))
    }
  }

  def addCommentReaction(): Action[JsValue] = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[UserCommentActivity].map { data =>
      commentService.addCommentActivityFromUser(request.identity.id, data.commentID.get, data) map { r =>
        Ok(Json.obj(
          "success" -> true,
          "data" -> r))
      }
    }.recoverTotal {
      error =>
        Future.successful(BadRequest(Json.obj(
          "success" -> false,
          "message" -> JsError.toJson(error))))
    }
  }

  def addComment(): Action[JsValue] = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[AddComment].map { data =>
      commentService.createComment(data, request.identity) map { r =>
        Ok(views.html.articles._comment(Some(request.identity), r))
      }
    }.recoverTotal {
      error =>
        Future.successful(BadRequest(JsError.toJson(error)))
    }
  }

  def editComment(): Action[JsValue] = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[EditComment].map { data =>
      commentService.editComment(data, request.identity) map { r =>
        Ok(Json.obj(
          "success" -> true,
          "data" -> r))
      }
    }.recoverTotal {
      error =>
        Future.successful(BadRequest(Json.obj(
          "success" -> false,
          "message" -> JsError.toJson(error))))
    }
  }

  def editSubComment(): Action[JsValue] = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[EditComment].map { data =>
      commentService.editSubComment(data, request.identity) map { r =>
        Ok(Json.obj(
          "success" -> true,
          "data" -> r))
      }
    }.recoverTotal {
      error =>
        Future.successful(BadRequest(Json.obj(
          "success" -> false,
          "message" -> JsError.toJson(error))))
    }
  }

  def addSubComment(): Action[JsValue] = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[AddSubComment].map { data =>
      commentService.createSubComment(data, request.identity) map { r =>
        Ok(views.html.articles._subComment(Some(request.identity), r))
      }
    }.recoverTotal {
      error =>
        Future.successful(BadRequest(JsError.toJson(error)))
    }
  }


  def getHotSubComments(articleID: String, commentID: String): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    commentService.getHotSubCommentsForComment(commentID, request.identity, 0, Integer.MAX_VALUE) map { r =>
      //      if (r.result.isEmpty) Ok(views.html.articles._articleNoMoreContents(request.identity, r))
      if (r.result.isEmpty) Ok("")
      else Ok(views.html.articles._subCommentList(request.identity, r))
    }
  }

  def getNewArticlesFromUser(userId: String, pageNumber: Int): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    articleService.getArticlesForUser(userId, pageNumber, Constants.DEFAULT_NO_OF_RECORDS) map { r =>
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
