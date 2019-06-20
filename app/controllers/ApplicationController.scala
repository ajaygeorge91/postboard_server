package controllers

import javax.inject.Inject

import actors.SubscribeActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasher}
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import forms._
import models.Article
import models.services.{ArticleService, LinkParsingService, UserNotificationService, UserService}
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import utils.Constants
import utils.auth.CookieEnv

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class ApplicationController @Inject()(
                                       implicit val system: ActorSystem,
                                       materializer: Materializer,
                                       val messagesApi: MessagesApi,
                                       authInfoRepository: AuthInfoRepository,
                                       avatarService: AvatarService,
                                       passwordHasher: PasswordHasher,
                                       userService: UserService,
                                       rssParsingService: LinkParsingService,
                                       userNotificationService: UserNotificationService,
                                       configuration: Configuration,
                                       clock: Clock,
                                       articleService: ArticleService,
                                       socialProviderRegistry: SocialProviderRegistry,
                                       silhouette: Silhouette[CookieEnv])
  extends Controller with I18nSupport {

  /**
    * Handles the index action.
    *
    * @return The result to display.
    */
  def index: Action[AnyContent] = silhouette.UserAwareAction.async { implicit request =>
    articleService.getHotArticlesUserOpt(request.identity, 0, Constants.DEFAULT_NO_OF_RECORDS) map { f =>
      Ok(views.html.home("Trending Posts | PostBoard", request.identity, f))
    }
  }


  def signIn = Action { implicit request =>
    Ok(views.html.signIn(SignInForm.form, SignUpForm.form, socialProviderRegistry))
  }


  def privacy = Action { implicit request =>
    Ok(views.html.privacy())
  }

  /**
    * Handles the Sign Out action.
    *
    * @return The result to display.
    */
  def signOut = silhouette.SecuredAction.async { implicit request =>
    val result = Redirect(routes.ApplicationController.index())
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }


  //ws

  //  def dashBoardSocket: WebSocket = ???
  def dashBoardSocket: WebSocket = WebSocket.acceptOrResult[String, String] { request =>
    implicit val req = Request(request, AnyContentAsEmpty)
    silhouette.SecuredRequestHandler { securedRequest =>
      Future.successful(HandlerResult(Ok, Some(securedRequest.identity)))
    }.map {
      case HandlerResult(r, Some(user)) =>
        Right(ActorFlow.actorRef(SubscribeActor.props(user, userNotificationService)))
      case HandlerResult(r, None) =>
        Left(r)
    }
  }


  //  override protected def onNotAuthenticated(request: RequestHeader): Option[Future[Result]] = {
  //    Some(Future.successful(Redirect(routes.ApplicationController.signIn())))
  //  }

}
