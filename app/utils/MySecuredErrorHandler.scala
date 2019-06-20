package utils

import javax.inject.Inject

import com.mohiva.play.silhouette.api.actions.SecuredErrorHandler
import controllers.routes
import play.api.http.DefaultHttpErrorHandler
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import play.api.routing.Router
import play.api.{Configuration, Logger, OptionalSourceMapper}

import scala.concurrent.Future

/**
  * A secured error handler.
  */
class MySecuredErrorHandler @Inject()(val messagesApi: MessagesApi) extends SecuredErrorHandler with I18nSupport {

  /**
    * Called when a user is not authenticated.
    *
    * As defined by RFC 2616, the status code of the response should be 401 Unauthorized.
    *
    * @param request The request header.
    * @return The result to send to the client.
    */
  override def onNotAuthenticated(implicit request: RequestHeader): Future[Result] = {
    Logger.logger.error(request.uri.toString)
    request.uri match {
      case u if u.startsWith("/api") || u.startsWith("/ajax") =>
        Future.successful(Unauthorized(Json.obj("success" -> false, "message" -> "UnAuthenticated")))
      case _ =>
        Future.successful(Redirect(routes.ApplicationController.signIn()))
    }
  }

  /**
    * Called when a user is authenticated but not authorized.
    *
    * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
    *
    * @param request The request header.
    * @return The result to send to the client.
    */
  override def onNotAuthorized(implicit request: RequestHeader): Future[Result] = {
    //    Future.successful(Unauthorized(Json.obj("success" -> false, "message" -> "Unauthorized")))
    Future.successful(Redirect(routes.ApplicationController.signIn()))
  }
}
