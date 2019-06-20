package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, Credentials}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import forms.{SignInForm, SignUpForm}
import models.User
import models.services.UserService
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import utils.auth.CookieEnv

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * The credentials auth controller.
  *
  */
class CredentialsAuthController @Inject()(
                                           val messagesApi: MessagesApi,
                                           userService: UserService,
                                           authInfoRepository: AuthInfoRepository,
                                           credentialsProvider: CredentialsProvider,
                                           socialProviderRegistry: SocialProviderRegistry,
                                           configuration: Configuration,
                                           clock: Clock,
                                           silhouette: Silhouette[CookieEnv])
  extends Controller with I18nSupport {
  /**
    * Authenticates a user against the credentials provider.
    *
    * @return The result to display.
    */
  def authenticate: Action[AnyContent] = Action.async { implicit request =>
    SignInForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signIn(form, SignUpForm.form, socialProviderRegistry))),
      data => {
        val credentials = Credentials(data.email, data.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          val result = Redirect(routes.ApplicationController.index())
          userService.retrieve(loginInfo).flatMap {
            case Some(user) =>
              val c = configuration.underlying
              silhouette.env.authenticatorService.create(loginInfo).map {
                case authenticator if data.rememberMe.getOrElse(false) =>
                  authenticator.copy(
                    expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
                    idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout"),
                    cookieMaxAge = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.cookieMaxAge")
                  )
                case authenticator => authenticator
              }.flatMap { authenticator =>
                silhouette.env.eventBus.publish(LoginEvent(user, request))
                silhouette.env.authenticatorService.init(authenticator).flatMap { v =>
                  silhouette.env.authenticatorService.embed(v, result)
                }
              }
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case e: ProviderException =>
            Redirect(routes.ApplicationController.signIn()).flashing("error" -> Messages("invalid.credentials"))
        }
      }
    )
  }

  //
  //  override protected def onNotAuthenticated(request: RequestHeader): Option[Future[Result]] = {
  //    Some(Future.successful(Redirect(routes.ApplicationController.signIn())))
  //  }


}
