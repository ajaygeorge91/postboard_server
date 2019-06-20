package controllers

import javax.inject.Inject

import actors.classifications.MessageBus
import akka.actor.ActorSystem
import com.mohiva.play.silhouette.api._
import forms._
import models.Article
import models.services._
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import utils.auth.CookieEnv
import utils.{Constants, StringUtils}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ArticleController @Inject()(
                                   val messagesApi: MessagesApi,
                                   implicit val system: ActorSystem,
                                   implicit val config: Configuration,
                                   messageBus: MessageBus,
                                   imageService: ImageService,
                                   articleService: ArticleService,
                                   rssParsingService: LinkParsingService,
                                   userService: UserService,
                                   commentService: CommentService,
                                   silhouette: Silhouette[CookieEnv])
  extends Controller with I18nSupport {


  def create: Action[MultipartFormData[TemporaryFile]] = silhouette.SecuredAction.async(parse.multipartFormData) { implicit request =>

    val imageKey_heightByWidth = request.body.file("image") match {
      case Some(image) if image.filename.nonEmpty =>
        import java.io.File
        val filename = image.filename

        //      val imageName = Images.processImage(image.ref.moveTo(new File(s"$filename")))
        val res = imageService.sendImage(image.ref.moveTo(new File(config.getString("myUploadPath").getOrElse("/opt/temp/") + filename)))
        Some(res)

      case _ => None
    }

    CreateArticleForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.articles.articleNew(form, forms.CreateArticleFromLinkForm.form, form.errorsAsJson.toString(), request.identity))),
      data => {

        articleService.createArticle(Article.getNewArticlePost( data.content, imageKey_heightByWidth.map(f => f._1), imageKey_heightByWidth.map(f => f._2)), request.identity) map { a =>
          Redirect(routes.ArticleController.details(a.id, None))
        }
      }
    )

  }

  def createFromLink: Action[MultipartFormData[TemporaryFile]] = silhouette.SecuredAction.async(parse.multipartFormData) { implicit request =>


    CreateArticleFromLinkForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.articles.articleNew(forms.CreateArticleForm.form, form, form.errorsAsJson.toString(), request.identity))),
      data => {
        rssParsingService.getArticleFromUrl(data.link) flatMap {
          case Some(artFromWeb) =>

            articleService.createArticle(artFromWeb, request.identity) map { a =>
              Redirect(routes.ArticleController.details(a.id, None))
            }
          case None =>
            Future.successful(Redirect(routes.ArticleController.createNew()))
        }
      }
    )

  }

  def list(pageNumber: Option[Int], numberOfRecords: Option[Int]): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    articleService.getArticlesUserOpt(request.identity, pageNumber.getOrElse(0), numberOfRecords.getOrElse(Constants.DEFAULT_NO_OF_RECORDS)) map { list =>
      Ok(views.html.home("New posts | PostBoard", request.identity, list))
    }
  }

  def createNew: Action[AnyContent] = silhouette.SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.articles.articleNew(forms.CreateArticleForm.form, forms.CreateArticleFromLinkForm.form, "New Post | PostBoard", request.identity)))
  }

  def details(articleID: String, highlightedComment: Option[String] = None): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    articleService.getArticleDetailsWithUserOpt(request.identity, articleID) flatMap {
      case Some(a) =>
        commentService.getHotCommentsForArticle(articleID, request.identity, 0, Constants.DEFAULT_NO_OF_RECORDS) flatMap { comments =>
          highlightedComment match {
            case Some(c) =>
              commentService.getCommentWithSubComments(c, request.identity) map { f =>
                Ok(views.html.articles.articleDetail(StringUtils.getTitle(a), request.identity, a, comments, linkedComment = f))
              }
            case None => Future.successful(Ok(views.html.articles.articleDetail(StringUtils.getTitle(a), request.identity, a, comments)))
          }
        }
      case None => Future.successful(NotFound("Not found"))
    }
  }

  //
  //  override protected def onNotAuthenticated(request: RequestHeader): Option[Future[Result]] = {
  //    Some(Future.successful(Redirect(routes.ApplicationController.signIn())))
  //  }


}
