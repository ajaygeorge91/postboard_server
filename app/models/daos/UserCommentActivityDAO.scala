package models.daos

import models.{UserCommentActivity, UserArticleActivity}

import scala.concurrent.Future

/**
  * Created by ajayg on 12/20/2016.
  */
trait UserCommentActivityDAO {

  def addCommentOrSubCommentReaction(userID: String, commentID: String, userCommentActivity: UserCommentActivity): Future[Option[UserCommentActivity]]

}
