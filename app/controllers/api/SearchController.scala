package controllers.api

import javax.inject.Inject

import actors.classifications.MessageBus
import akka.actor.ActorSystem
import com.mohiva.play.silhouette.api._
import models.services._
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import utils.Constants
import utils.auth.JwtEnv

import scala.concurrent.ExecutionContext.Implicits.global

class SearchController @Inject()(
                                  val messagesApi: MessagesApi,
                                  messageBus: MessageBus,
                                  articleService: ArticleService,
                                  searchService: SearchService,
                                  commentService: CommentService,
                                  implicit val system: ActorSystem,
                                  implicit val config: Configuration,
                                  imageService: ImageService,
                                  userService: UserService,
                                  silhouette: Silhouette[JwtEnv])
  extends Controller with I18nSupport {

  def getSearchBundle(searchKey: String): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    searchService.getSearchBundleFromKey(request.identity, searchKey) map { f =>
      Ok(Json.obj(
        "success" -> true,
        "data" -> f))
    }
  }

  def getArticleSearchResults(searchKey: String, pageNumber: Option[Int], numberOfRecords: Option[Int]): Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    searchService.getArticlesFromQuery(request.identity, searchKey, pageNumber.getOrElse(0), numberOfRecords.getOrElse(Constants.DEFAULT_NO_OF_RECORDS)) map { f =>
      Ok(Json.obj(
        "success" -> true,
        "data" -> f))
    }
  }

}
