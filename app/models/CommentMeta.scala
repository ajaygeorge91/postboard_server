package models

import play.api.libs.json.Json

/**
  * Created by ajayg on 12/20/2016.
  */
case class CommentMeta(
                        ratingSum: Option[Int] = None,
                        subCommentCount: Option[Int] = None,
                        value: Option[Float] = None
                      )


object CommentMeta {
  implicit val jsonFormat = Json.format[CommentMeta]

  def getNone: CommentMeta = {
    CommentMeta(ratingSum = None)
  }
}

