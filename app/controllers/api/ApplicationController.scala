package controllers.api

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models.Image
import models.services.ImageService
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{Action, Controller, MultipartFormData, RequestHeader}
import utils.auth.JwtEnv

import scala.concurrent.{ExecutionContext, Future}

/**
  * The basic application controller.
  *
  */
class ApplicationController @Inject()(
                                       val messagesApi: MessagesApi,
                                       implicit val config: Configuration,
                                       imageService: ImageService,
                                       socialProviderRegistry: SocialProviderRegistry,
                                       silhouette: Silhouette[JwtEnv])(implicit ec: ExecutionContext)
  extends Controller with I18nSupport {


  def createImageEntry: Action[MultipartFormData[TemporaryFile]] = silhouette.SecuredAction.async(parse.multipartFormData) { implicit request =>
    request.body.file("image").map { image =>
      import java.io.File
      val filename = image.filename

      val (imageKey, heightByWidth) = imageService.sendImage(image.ref.moveTo(new File(config.getString("myUploadPath").getOrElse("/opt/temp/") + filename)))

      Future.successful(Ok(Json.obj(
        "success" -> true,
        "data" -> Image(
          imageKey = Some(imageKey),
          heightByWidth = Some(heightByWidth)
        )
      )))
    }.getOrElse {
      Future.successful(Ok("error"))
    }

  }

  /**
    * Returns the user.
    *
    * @return The result to display.
    */
  def user = silhouette.SecuredAction.async { implicit request =>
    Future.successful(
      Ok(Json.obj(
        "success" -> true,
        "data" -> request.identity))
    )
  }

  /**
    * Returns the user or false..(UserAwareAction).
    *
    * @return The result to display.
    */
  def userOpt = silhouette.UserAwareAction.async { implicit request =>
    Future.successful(
      request.identity match {
        case Some(u) =>
          Ok(Json.obj(
            "success" -> true,
            "data" -> u))
        case None =>
          Ok(Json.obj(
            "success" -> false,
            "message" -> "Nop.."))
      }
    )
  }

  /**
    * Manages the sign out action.
    */
  def signOut = silhouette.SecuredAction.async { implicit request =>
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, Ok)
  }

}
