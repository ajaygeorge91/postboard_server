package controllers.api

import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ExtractableRequest, HTTPLayer}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth2.FacebookProvider
import models.{AuthResultDTO, User}
import models.services.UserService
import play.api.cache.CacheApi
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.mvc._
import utils.auth.JwtEnv

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * The social auth controller.
  *
  */
class SocialAuthController @Inject()(
                                      val messagesApi: MessagesApi,
                                      silhouette: Silhouette[JwtEnv],
                                      userService: UserService,
                                      authInfoRepository: AuthInfoRepository,
                                      socialProviderRegistry: SocialProviderRegistry,
                                      cache: CacheApi)
  extends Controller with I18nSupport with Logger {


  /**
    * Authenticates a user against a social provider.
    *
    * @param provider The ID of the provider to authenticate against.
    * @return The result to display.
    */
  def authenticate(provider: String) = Action.async { implicit request =>
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            user <- userService.save(profile, provider)
            authInfo <- authInfoRepository.save(profile.loginInfo, authInfo)
            authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
            token <- silhouette.env.authenticatorService.init(authenticator)
          } yield {
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            val result = AuthResultDTO(token, Some(user))
            Ok(Json.obj(
              "success" -> true,
              "data" -> result))
          }
        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Ok(Json.obj(
          "success" -> false,
          "message" -> Messages("could.not.authenticate")))
    }
  }

  def authenticateFB() = Action.async(parse.json) {
    implicit request =>
      request.body.asOpt[OAuth2Info] match {
        case Some(authInfo) =>
          (socialProviderRegistry.get[FacebookProvider]("facebook") match {
            case Some(p: FacebookProvider) =>
              for {
                profile <- p.retrieveProfile(authInfo)
                user <- userService.save(profile, "facebook")
                authInfo <- authInfoRepository.save(profile.loginInfo, authInfo)
                authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
                token <- silhouette.env.authenticatorService.init(authenticator)
              } yield {
                silhouette.env.eventBus.publish(LoginEvent(user, request))
                val result = AuthResultDTO(token, Some(user))
                Ok(Json.obj(
                  "success" -> true,
                  "data" -> result))
              }
            case _ => Future.failed(new ProviderException(s"Cannot authenticate with facebook"))
          }).recover {
            case e: ProviderException =>
              logger.error("Unexpected provider error", e)
              Unauthorized(Json.obj("message" -> Messages("could.not.authenticate")))
          }
        case _ =>
          Future(BadRequest(Json.obj(
            "message" -> "Bad OAuth2 json.")))

      }
  }

}
