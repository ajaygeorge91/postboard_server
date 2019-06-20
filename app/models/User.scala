package models

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import play.api.libs.json.Json

case class User(
                 id: String,
                 userType: Option[String] = None,
                 loginInfo: Option[LoginInfo] = None,
                 fullName: Option[String] = None,
                 avatarURL: Option[String] = None,
                 userProfile: Option[UserProfile] = None) extends Identity

object User {
  implicit val jsonFormat = Json.format[User]
}
