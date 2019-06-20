package models

import play.api.libs.json.Json

/**
  * Created by ajayg on 12/20/2016.
  */
case class SubCommentMeta(
                        ratingSum: Option[Int] = None
                      )


object SubCommentMeta {
  implicit val jsonFormat = Json.format[SubCommentMeta]

  def getNone: SubCommentMeta = {
    SubCommentMeta(ratingSum = None)
  }
}

