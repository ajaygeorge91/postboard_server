package controllers.api

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.SignUpForm
import models.{AuthResultDTO, ProfileAvatar, User, UserProfile}
import models.services.UserService
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import utils.auth.JwtEnv
import utils.{Constants, ProfileUtils, UuidUtils}

import scala.concurrent.Future

/**
  * The sign up controller.
  *
  * @param messagesApi        The Play messages API.
  * @param env                The Silhouette environment.
  * @param userService        The user service implementation.
  * @param authInfoRepository The auth info repository implementation.
  * @param avatarService      The avatar service implementation.
  * @param passwordHasher     The password hasher implementation.
  */
class SignUpController @Inject()(
                                  val messagesApi: MessagesApi,
                                  userService: UserService,
                                  authInfoRepository: AuthInfoRepository,
                                  avatarService: AvatarService,
                                  passwordHasher: PasswordHasher,
                                  silhouette: Silhouette[JwtEnv])
  extends Controller with I18nSupport {

  /**
    * Registers a new user.
    *
    * @return The result to display.
    */
  def signUp = Action.async(parse.json) { implicit request =>
    request.body.validate[SignUpForm.Data].map { data =>
      val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
      userService.retrieve(loginInfo).flatMap {
        case Some(user) =>
          Future.successful(
            Ok(Json.obj(
              "success" -> false,
              "message" -> Messages("user.exists"))
            )
          )
        case None =>
          val authInfo = passwordHasher.hash(data.password)
          val userProfile = UserProfile(
            email = Some(data.email)
          )
          val user = User(
            id = UuidUtils.getUUID,
            loginInfo = Some(loginInfo),
            fullName = Some(data.fullName),
            avatarURL = None,
            userProfile = Some(userProfile)
          )
          for {
            avatar <- avatarService.retrieveURL(data.email)
            user <- userService.save(user.copy(avatarURL = avatar, userProfile = Some(ProfileUtils.addAvatar(ProfileAvatar(Constants.GRAVATAR, avatar), Some(userProfile)))))
            authInfo <- authInfoRepository.add(loginInfo, authInfo)
            authenticator <- silhouette.env.authenticatorService.create(loginInfo)
            token <- silhouette.env.authenticatorService.init(authenticator)
          } yield {
            silhouette.env.eventBus.publish(SignUpEvent(user, request))
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            val result = AuthResultDTO(token, Some(user))
            Ok(Json.obj(
              "success" -> true,
              "data" -> result))
          }
      }
    }.recoverTotal {
      case error =>
        Future.successful(Unauthorized(Json.obj("message" -> Messages("invalid.data"))))
    }
  }

}
