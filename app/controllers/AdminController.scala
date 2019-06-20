package controllers

import javax.inject.Inject

import actors.SubscribeActor
import actors.bots.BotUserActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasher}
import com.mohiva.play.silhouette.impl.providers.{CredentialsProvider, SocialProviderRegistry}
import forms._
import models.{ProfileAvatar, User, UserProfile}
import models.services.{ArticleService, LinkParsingService, UserNotificationService, UserService}
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import utils.{Constants, ProfileUtils}
import utils.auth.CookieEnv

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

class AdminController @Inject()(
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


  def admin = silhouette.SecuredAction.async { implicit request =>
    if (request.identity.userType.getOrElse("").equalsIgnoreCase(Constants.USER_TYPE_ADMIN)) {
      userService.getRssFeedUsers map { uList =>
        Ok(views.html.admin.rss(uList, forms.AddUrlForm.form))
      }
    } else {
      Future.successful(Ok(views.html.messages("admin only", Some(request.identity), "admin only")))
    }
  }

  def rssPost: Action[AnyContent] = silhouette.SecuredAction.async { implicit request =>
    if (request.identity.userType.getOrElse("").equalsIgnoreCase(Constants.USER_TYPE_ADMIN)) {
      AddUrlForm.form.bindFromRequest.fold(
        form => Future.successful(BadRequest(views.html.admin.rss(List.empty, form))),
        data => {
          val rssUser = userService.getUserFromRssUrl(data.url) flatMap {
            case Some(user) =>
              Future.successful(user)
            case None =>
              val email = "rss" + data.name.replace(" ", "").toLowerCase + "@postboard.online"
              val loginInfo = LoginInfo(CredentialsProvider.ID, email)
              val authInfo = passwordHasher.hash(Constants.DEFAULT_PASSWORD)
              val userProfile = UserProfile(
                email = Some(email),
                rssUrl = Some(data.url)
              )
              val user = User(
                id = "",
                userType = Some(Constants.USER_TYPE_RSS_FEED),
                loginInfo = Some(loginInfo),
                fullName = Some(data.name),
                avatarURL = None
              )
              for {
                avatar <- avatarService.retrieveURL(email)
                userResult <- userService.save(user.copy(avatarURL = avatar, userProfile = Some(ProfileUtils.addAvatar(ProfileAvatar("gravatar", avatar), Some(userProfile)))))
                authInfo <- authInfoRepository.add(loginInfo, authInfo)
              } yield {
                userResult
              }
          }
          rssUser map { u =>
            Ok(views.html.messages("created", Some(request.identity), "created"))
          }
        }
      )
    } else {
      Future.successful(Ok(views.html.messages("admin only", Some(request.identity), "admin only")))
    }
  }


}
