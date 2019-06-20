package utils

import models.{ProfileAvatar, UserProfile}

/**
  * Created by Android SD-1 on 16-03-2017.
  */
object ProfileUtils {

  def addAvatar(profileAvatar: ProfileAvatar, userProfile: Option[UserProfile]): UserProfile = {
    userProfile.getOrElse(UserProfile()).avatars.map { avatarList =>
      avatarList.find(a => a.provider.equalsIgnoreCase(profileAvatar.provider)) match {
        case Some(x) =>
          val newList = avatarList.map { a =>
            if (a.provider.equalsIgnoreCase(profileAvatar.provider) && profileAvatar.url.getOrElse("").nonEmpty) {
              a.copy(url = profileAvatar.url)
            } else {
              a
            }
          }
          userProfile.getOrElse(UserProfile()).copy(avatars = Some(newList))
        case None =>
          userProfile.getOrElse(UserProfile()).copy(avatars = Some(avatarList :+ profileAvatar))
      }
    }.getOrElse {
      if (profileAvatar.url.getOrElse("").nonEmpty) {
        userProfile.getOrElse(UserProfile()).copy(avatars = Some(List(profileAvatar)))
      } else {
        userProfile.getOrElse(UserProfile())
      }
    }
  }

}
