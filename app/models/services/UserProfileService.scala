package models.services

import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.{UserActivity, PaginatedResult, UserAction, User}

import scala.concurrent.Future

/**
  * Handles actions to users.
  */
trait UserProfileService {

  def getActions(userId:String, pageNumber: Int, numberOfRecords: Int): Future[PaginatedResult[UserActivity]]
}
