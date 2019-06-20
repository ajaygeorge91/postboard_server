package utils.auth

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.User

/**
  * Created by Android SD-1 on 07-04-2017.
  */


/**
  * The default env.
  */
trait CookieEnv extends Env {
  type I = User
  type A = CookieAuthenticator
}