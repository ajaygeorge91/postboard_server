package models

import play.api.libs.json.Json

/**
  * Created by ajayg on 12/20/2016.
  */
case class ArticleMeta(
                        ratingSum: Option[Long] = None,
                        commentCount: Option[Long] = None,
                        rank: Option[Double] = None
                      )

object ArticleMeta {
  implicit val jsonFormat = Json.format[ArticleMeta]
}