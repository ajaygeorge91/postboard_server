package controllers.api

import javax.inject.Inject

import actors.classifications.MessageBus
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models.services.{ArticleService, CommentService, UserService}
import models._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.{Action, AnyContent, Controller}
import utils.Constants
import utils.auth.JwtEnv

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CommentController @Inject()(
                                   val messagesApi: MessagesApi,
                                   messageBus: MessageBus,
                                   articleService: ArticleService,
                                   commentService: CommentService,
                                   userService: UserService,
                                   silhouette: Silhouette[JwtEnv])
  extends Controller with I18nSupport {



  def addComment(articleID: String): Action[JsValue] = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[AddComment].map { data =>
      commentService.createComment(data, request.identity) map { r =>
        Ok(Json.obj(
          "success" -> true,
          "data" -> r))
      }
    }.recoverTotal {
      error =>
        Future.successful(BadRequest(Json.obj(
          "success" -> false,
          "message" -> "invalid data")))
    }
  }

  def editComment(articleID: String): Action[JsValue] = silhouette.SecuredAction.async(parse.json) { implicit request =>
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

  def addCommentReaction(articleID: String, commentId: String): Action[JsValue] = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[UserCommentActivity].map { data =>
      commentService.addCommentActivityFromUser(request.identity.id, data.commentID.getOrElse(commentId), data) map { r =>
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


  def getHotComments(articleID: String, pageNumber: Option[Int], numberOfRecords: Option[Int]): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    commentService.getHotCommentsForArticle(articleID, request.identity, pageNumber.getOrElse(0), numberOfRecords.getOrElse(Constants.DEFAULT_NO_OF_RECORDS)) map { r =>
      Ok(Json.obj(
        "success" -> true,
        "data" -> r))
    }
  }

  def getNewComments(articleID: String, pageNumber: Option[Int], numberOfRecords: Option[Int]): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    commentService.getNewCommentsForArticle(articleID, request.identity, pageNumber.getOrElse(0), numberOfRecords.getOrElse(Constants.DEFAULT_NO_OF_RECORDS)) map { r =>
      Ok(Json.obj(
        "success" -> true,
        "data" -> r))
    }
  }

  def addSubComment(articleID: String, commentID: String): Action[JsValue] = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[AddSubComment].map { data =>
      commentService.createSubComment(data, request.identity) map { r =>
        Ok(Json.obj(
          "success" -> true,
          "data" -> r))
      }
    }.recoverTotal {
      error =>
        Future.successful(Ok(Json.obj(
          "success" -> true,
          "data" -> JsError.toJson(error))))
    }
  }

  def editSubComment(articleID: String, commentID: String): Action[JsValue] = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[EditComment].map { data =>
      commentService.editSubComment(data, request.identity) map { r =>
        Ok(Json.obj(
          "success" -> true,
          "data" -> r))
      }
    }.recoverTotal {
      error =>
        Future.successful(Ok(Json.obj(
          "success" -> true,
          "data" -> JsError.toJson(error))))
    }
  }

  def getHotSubComments(articleID: String, commentID: String, pageNumber: Option[Int], numberOfRecords: Option[Int]): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    commentService.getHotSubCommentsForComment(commentID, request.identity, pageNumber.getOrElse(0), numberOfRecords.getOrElse(Integer.MAX_VALUE)) map { r =>
      Ok(Json.obj(
        "success" -> true,
        "data" -> r))
    }
  }

}
