package controllers.api

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, Credentials}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import forms.SignInForm
import models.{AuthResultDTO, User}
import models.services.UserService
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import utils.auth.JwtEnv

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * The credentials auth controller.
  */
class CredentialsAuthController @Inject()(
                                           val messagesApi: MessagesApi,
                                           userService: UserService,
                                           authInfoRepository: AuthInfoRepository,
                                           credentialsProvider: CredentialsProvider,
                                           socialProviderRegistry: SocialProviderRegistry,
                                           configuration: Configuration,
                                           clock: Clock,
                                           silhouette: Silhouette[JwtEnv])
  extends Controller with I18nSupport {

  /**
    * Authenticates a user against the credentials provider.
    *
    * @return The result to display.
    */
  def authenticate = Action.async(parse.json) { implicit request =>
    request.body.validate[SignInForm.Data].map { data =>
      credentialsProvider.authenticate(Credentials(data.email, data.password)).flatMap { loginInfo =>
        userService.retrieve(loginInfo).flatMap {
          case Some(user) => silhouette.env.authenticatorService.create(loginInfo).map {
            case authenticator if data.rememberMe.getOrElse(false) =>
              val c = configuration.underlying
              authenticator.copy(
                expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
                idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout")
              )
            case authenticator => authenticator
          }.flatMap { authenticator =>
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            silhouette.env.authenticatorService.init(authenticator).map { token =>
              val result = AuthResultDTO(token, Some(user))
              Ok(Json.obj(
                "success" -> true,
                "data" -> result))
            }
          }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }
      }.recover {
        case e: ProviderException =>
          //          Unauthorized(Json.obj("message" -> Messages("invalid.credentials")))
          Ok(Json.obj(
            "success" -> false,
            "message" -> Messages("invalid.credentials")))
      }
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.obj(
          "success" -> false,
          "message" -> Messages("invalid.credentials"))))
      //        Future.successful(Unauthorized(Json.obj("message" -> Messages("invalid.credentials"))))
    }
  }


}
