package models

import java.util.UUID

import org.joda.time.DateTime
import play.api.libs.json.{Json, OFormat}
import utils.{StringUtils, UuidUtils}

/**
  * Created by ajayg on 10/2/2016.
  */
case class Comment(
                    id: String,
                    content: Option[String] = None,
                    createdAt: Option[String] = None,
                    createdBy: Option[User] = None,
                    commentMeta: Option[CommentMeta] = None,
                    userCommentActivity: Option[UserCommentActivity] = None,
                    articleID: Option[String] = None,
                    subCommentList: Option[List[SubComment]] = None,
                    subCommentListP: Option[PaginatedResult[SubComment]] = None
                  )


object Comment {
  implicit val jsonFormat: OFormat[Comment] = Json.format[Comment]

  def get(content: String): Comment = {
    Comment(
      id = UuidUtils.getUUID,
      content = Some(StringUtils.getTrimmedContent(content)),
      createdAt = Some(DateTime.now().toString)
    )
  }
}

