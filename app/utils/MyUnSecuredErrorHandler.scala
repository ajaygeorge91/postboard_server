package utils

import javax.inject.Inject

import controllers.routes

import scala.concurrent.Future
import com.mohiva.play.silhouette.api.actions.UnsecuredErrorHandler
import controllers.routes
import play.api.http.DefaultHttpErrorHandler
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import play.api.routing.Router
import play.api.{Configuration, Logger, OptionalSourceMapper}


/**
  * A secured error handler.
  */
class MyUnSecuredErrorHandler  extends UnsecuredErrorHandler {

  /**
    * Called when a user is authenticated but not authorized.
    *
    * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
    *
    * @param request The request header.
    * @return The result to send to the client.
    */
  override def onNotAuthorized(implicit request: RequestHeader) = {
    Future.successful(Redirect(routes.ApplicationController.signIn()))
  }
}