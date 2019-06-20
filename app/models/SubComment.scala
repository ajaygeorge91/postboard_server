package models

import java.util.UUID

import org.joda.time.DateTime
import play.api.libs.json.Json
import utils.{StringUtils, UuidUtils}

/**
  * Created by ajayg on 10/2/2016.
  *
  *
  */
case class SubComment(
                       id: String,
                       content: Option[String] = None,
                       createdAt: Option[String] = None,
                       createdBy: Option[User] = None,
                       subCommentMeta: Option[SubCommentMeta] = None,
                       userSubCommentActivity: Option[UserCommentActivity] = None,
                       commentID: Option[String] = None
                     )


object SubComment {
  implicit val jsonFormat = Json.format[SubComment]

  def get(content: String) = {
    SubComment(
      id = UuidUtils.getUUID,
      content = Some(StringUtils.getTrimmedContent(content)),
      createdAt = Some(DateTime.now().toString)
    )
  }
}



