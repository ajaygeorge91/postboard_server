package utils.auth

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models.User

/**
  * Created by Android SD-1 on 07-04-2017.
  */
trait  JwtEnv extends Env {
  type I = User
  type A = JWTAuthenticator
}