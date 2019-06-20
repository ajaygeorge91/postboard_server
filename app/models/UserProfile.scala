package models

import com.mohiva.play.silhouette.api.LoginInfo
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._


case class UserProfile(
                        loginInfos: Option[List[LoginInfo]] = None,
                        avatars: Option[List[ProfileAvatar]] = None,
                        email: Option[String] = None,
                        rssUrl: Option[String] = None
                      )


object UserProfile {

  implicit val jsonFormat = Json.format[UserProfile]

  def getAvatarsAsStringList(avatars: List[ProfileAvatar]): List[String] = {
    avatars.map(a => ProfileAvatar.getAsString(a))
  }

  def getAvatarsFromStringList(avatars: List[String]): List[ProfileAvatar] = {
    avatars.map(a => ProfileAvatar.getFromString(a))
  }

}

