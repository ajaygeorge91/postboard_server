package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.providers._
import forms.{SignInForm, SignUpForm}
import models.services.UserService
import models.{ProfileAvatar, User, UserProfile}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{Action, AnyContent, Controller}
import utils.{Constants, ProfileUtils, UuidUtils}
import utils.auth.CookieEnv

import scala.concurrent.Future

/**
  * The sign up controller.
  */
class SignUpController @Inject()(
                                  val messagesApi: MessagesApi,
                                  userService: UserService,
                                  authInfoRepository: AuthInfoRepository,
                                  socialProviderRegistry: SocialProviderRegistry,
                                  avatarService: AvatarService,
                                  passwordHasher: PasswordHasher,
                                  silhouette: Silhouette[CookieEnv])
  extends Controller with I18nSupport {
  /**
    * Registers a new user.
    *
    * @return The result to display.
    */
  def signUp: Action[AnyContent] = Action.async { implicit request =>
    SignUpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signIn(SignInForm.form, form, socialProviderRegistry))),
      data => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            Future.successful(Redirect(routes.ApplicationController.signIn()).flashing("error" -> Messages("user.exists")))
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
              user <- userService.save(user.copy(avatarURL = avatar, userProfile = Some(ProfileUtils.addAvatar(ProfileAvatar("gravatar", avatar), Some(userProfile)))))
              authInfo <- authInfoRepository.add(loginInfo, authInfo)
              authenticator <- silhouette.env.authenticatorService.create(loginInfo)
              value <- silhouette.env.authenticatorService.init(authenticator)
              result <- silhouette.env.authenticatorService.embed(value, Redirect(routes.ApplicationController.index()))
            } yield {
              silhouette.env.eventBus.publish(SignUpEvent(user, request))
              silhouette.env.eventBus.publish(LoginEvent(user, request))
              result
            }
        }
      }
    )
  }

}
