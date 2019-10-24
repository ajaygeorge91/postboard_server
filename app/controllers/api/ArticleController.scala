package controllers.api

import javax.inject.Inject

import actors.classifications.MessageBus
import akka.actor.ActorSystem
import com.mohiva.play.silhouette.api._
import forms.CreateArticleForm
import models._
import models.services.{ArticleService, CommentService, ImageService, UserService}
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.{Action, AnyContent, Controller, MultipartFormData}
import utils.Constants
import utils.auth.JwtEnv

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ArticleController @Inject()(
                                   val messagesApi: MessagesApi,
                                   messageBus: MessageBus,
                                   articleService: ArticleService,
                                   commentService: CommentService,
                                   implicit val system: ActorSystem,
                                   implicit val config: Configuration,
                                   imageService: ImageService,
                                   userService: UserService,
                                   silhouette: Silhouette[JwtEnv])
  extends Controller with I18nSupport {


  def createArticle: Action[MultipartFormData[TemporaryFile]] = silhouette.SecuredAction.async(parse.multipartFormData) { implicit request =>
    val imageKey_heightByWidth = request.body.file("image") match {
      case Some(image) if image.filename.nonEmpty =>
        import java.io.File
        val filename = image.filename
        val res = imageService.sendImage(image.ref.moveTo(new File(config.getString("myUploadPath").getOrElse("/opt/temp/") + filename)))
        Some(res)

      case _ => None
    }


    CreateArticleForm.form.bindFromRequest.fold(
      form =>
        Future.successful(Ok(Json.obj(
          "success" -> false,
          "message" -> form.errorsAsJson.toString()))),
      data => {
        articleService.createArticle(Article.getNewArticlePost(data.title, data.content, imageKey_heightByWidth.map(f => f._1), imageKey_heightByWidth.map(f => f._2)), request.identity) map { a =>
          Ok(Json.obj(
            "success" -> true,
            "data" -> a
          ))
        }
      }
    )

  }


  def create(): Action[JsValue] = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[Article].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("success" -> false,
          "message" -> JsError.toJson(errors))))
      },
      article => {
        articleService.createArticle(article, request.identity) map { a =>
          Ok(Json.obj(
            "success" -> true,
            "data" -> a))
        }
      }
    )
  }

  def list(pageNumber: Option[Int], numberOfRecords: Option[Int]): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    articleService.getArticlesUserOpt(request.identity, pageNumber.getOrElse(0), numberOfRecords.getOrElse(Constants.DEFAULT_NO_OF_RECORDS)) map { list =>
      Ok(Json.obj(
        "success" -> true,
        "data" -> list))
    }
  }

  def getHotArticles(pageNumber: Option[Int], numberOfRecords: Option[Int]): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    articleService.getHotArticlesUserOpt(request.identity, pageNumber.getOrElse(0), Constants.DEFAULT_NO_OF_RECORDS) map { r =>
      Ok(Json.obj(
        "success" -> true,
        "data" -> r))
    }
  }

  def getNewArticles(pageNumber: Option[Int], numberOfRecords: Option[Int]): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    articleService.getArticlesUserOpt(request.identity, pageNumber.getOrElse(0), Constants.DEFAULT_NO_OF_RECORDS) map { r =>
      Ok(Json.obj(
        "success" -> true,
        "data" -> r))
    }
  }

  def details(articleID: String): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    articleService.getArticleDetailsWithUserOpt(request.identity, articleID) flatMap {
      case Some(o) =>
        commentService.getHotCommentsForArticle(articleID, request.identity, 0, Constants.DEFAULT_NO_OF_RECORDS) map { comments =>
          Ok(Json.obj(
            "success" -> true,
            "data" -> o.copy(comments = Some(comments))))
        }
      case None => Future.successful(Ok(Json.obj(
        "success" -> false,
        "message" -> "Not found")))
    }
  }

  def getArticleFromNodeID(nodeID: String): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    articleService.getNodeType(nodeID) flatMap {
      case "Article" =>
        articleService.getArticleDetailsWithUserOpt(request.identity, nodeID) flatMap {
          case Some(o) =>
            commentService.getHotCommentsForArticle(nodeID, request.identity, 0, Constants.DEFAULT_NO_OF_RECORDS) map { comments =>
              Ok(Json.obj(
                "success" -> true,
                "data" -> o.copy(comments = Some(comments))))
            }
          case None => Future.successful(Ok(Json.obj(
            "success" -> false,
            "message" -> "Not found")))
        }
      case "Comment" =>
        commentService.getCommentWithSubComments(nodeID, request.identity) flatMap {
          case Some(o) =>
            articleService.getArticleDetailsWithUserOpt(request.identity, o.articleID.getOrElse("")) flatMap {
              case Some(a) =>
                commentService.getNewCommentsForArticle(o.articleID.getOrElse(""), request.identity, 0, Constants.DEFAULT_NO_OF_RECORDS) map { comments =>
                  Ok(Json.obj(
                    "success" -> true,
                    "data" -> a.copy(comments = Some(comments), linkedComment = Some(o))))
                }
              case None => Future.successful(Ok(Json.obj(
                "success" -> false,
                "message" -> "Not found")))
            }
          case None => Future.successful(Ok(Json.obj(
            "success" -> false,
            "message" -> "Not found")))
        }
      case _ =>
        Future.successful(Ok(Json.obj(
          "success" -> false,
          "message" -> "Not found")))
    }
  }

  def getHotArticleReactionForFirstNPages(page: Int): Action[AnyContent] = silhouette.SecuredAction.async { implicit request =>
    articleService.getHotArticleReactionForFirstNPages(request.identity, page) map { f =>
      Ok(Json.obj(
        "success" -> true,
        "data" -> f))
    }
  }

  def getNewArticleReactionForFirstNPages(page: Int): Action[AnyContent] = silhouette.SecuredAction.async { implicit request =>
    articleService.getNewArticleReactionForFirstNPages(request.identity, page) map { f =>
      Ok(Json.obj(
        "success" -> true,
        "data" -> f))
    }
  }

  def getArticleReaction(articleID: String): Action[AnyContent] = silhouette.SecuredAction.async { implicit request =>
    articleService.getArticleActivity(request.identity, articleID) map {
      case Some(f) =>
        Ok(Json.obj(
          "success" -> true,
          "data" -> f))
      case None => Ok(Json.obj(
        "success" -> false,
        "message" -> "Not found"))
    }
  }

  def addArticleReaction(articleID: String): Action[JsValue] = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[UserArticleActivity].map { data =>
      articleService.addArticleActivityFromUser(request.identity.id.toString, articleID, data) map { r =>
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


}
