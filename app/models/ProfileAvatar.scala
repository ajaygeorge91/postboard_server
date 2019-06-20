package models

import play.api.libs.json.Json
import utils.Constants


case class ProfileAvatar(
                          provider: String,
                          url: Option[String] = None
                        )

object ProfileAvatar {
  implicit val jsonFormat = Json.format[ProfileAvatar]

  def getAsString(profileAvatar: ProfileAvatar): String = {
    profileAvatar.provider + Constants.AVATAR_SEPARATOR + profileAvatar.url.getOrElse("")
  }

  def getFromString(profileAvatarString: String): ProfileAvatar = {
    ProfileAvatar(provider = profileAvatarString.split(Constants.AVATAR_SEPARATOR).headOption.getOrElse(""),
      url = profileAvatarString.split(Constants.AVATAR_SEPARATOR).lastOption)
  }

}
