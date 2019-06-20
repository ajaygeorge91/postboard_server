package models.services

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.{ProfileAvatar, User, UserProfile}
import models.daos.UserDAO
import play.api.libs.concurrent.Execution.Implicits._
import utils.{ProfileUtils, UuidUtils}

import scala.concurrent.Future

/**
  * Handles actions to users.
  *
  * @param userDAO The user DAO implementation.
  */
class UserServiceImpl @Inject()(userDAO: UserDAO) extends UserService {

  /**
    * Retrieves a user that matches the specified login info.
    *
    * @param loginInfo The login info to retrieve a user.
    * @return The retrieved user or None if no user could be retrieved for the given login info.
    */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  /**
    * Saves a user.
    *
    * @param user The user to save.
    * @return The saved user.
    */
  def save(user: User): Future[User] = userDAO.save(user)

  def getFromID(userID: String): Future[Option[User]] = {
    userDAO.find(userID)
  }

  def getRssFeedUsers: Future[List[User]] = {
    userDAO.getRssFeedUsers
  }

  def getUserFromRssUrl(rssUrl: String): Future[Option[User]] = {
    userDAO.getUserFromRssUrl(rssUrl)
  }

  def getProfileFromID(userID: String): Future[Option[User]] = {
    userDAO.findPrivateUserProfile(userID)
  }

  /**
    * Saves the social profile for a user.
    *
    * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
    *
    * @param profile The social profile to save.
    * @return The user for whom the profile was saved.
    */
  def save(profile: CommonSocialProfile, provider: String) = {
    // Update user with profile
    userDAO.find(profile.loginInfo).flatMap {
      case Some(user) =>
        //TODO update profile
        //      val oldUserProfile = user.userProfile
        //        val newUserProfile = ProfileUtils.addAvatar(ProfileAvatar(provider, profile.avatarURL), oldUserProfile).copy(email = profile.email)
        //        userDAO.update(user.copy(
        //          firstName = profile.firstName,
        //          lastName = profile.lastName,
        //          fullName = profile.fullName,
        //          userProfile = Some(newUserProfile),
        //          avatarURL = profile.avatarURL
        //        ))
        Future.successful(user)
      case None => // Insert a new user
        val userProfile = UserProfile(
          email = profile.email,
          avatars = Some(List(ProfileAvatar(provider, profile.avatarURL)))
        )
        userDAO.save(User(
          id = UuidUtils.getUUID,
          loginInfo = Some(profile.loginInfo),
          fullName = profile.fullName,
          avatarURL = profile.avatarURL,
          userProfile = Some(userProfile)
        ))
    }
  }

}
