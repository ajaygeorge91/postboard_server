package models

import org.joda.time.DateTime
import play.api.libs.json.{Json, OFormat}
import utils.{StringUtils, UuidUtils}

/**
  * Created by ajayg on 10/2/2016.
  */

case class Article(
                    id: String,
                    articleType: Option[String] = None,
                    title: Option[String] = None,
                    content: Option[String] = None,
                    image: Option[Image] = None,
                    imageString: Option[String] = None,
                    createdAt: Option[String] = None,
                    updatedAt: Option[String] = None,
                    link: Option[String] = None,
                    userArticleActivity: Option[UserArticleActivity] = None,
                    createdBy: Option[User] = None,
                    articleMeta: Option[ArticleMeta] = None,
                    comments: Option[PaginatedResult[Comment]] = None,
                    linkedComment: Option[Comment] = None
                  )

object Article {

  implicit val jsonFormat: OFormat[Article] = Json.format[Article]

  def getNewArticlePostSample(title: Option[String], content: Option[String], imageKey: Option[String], heightByWidth: Option[Float]): Article = {
    val now = DateTime.now().toString
    Article(
      id = UuidUtils.getUUID,
      articleType = Some(ARTICLE_TYPE_POST),
      content = content.map(f => StringUtils.getTrimmedContent(f)),
      image = Image.getOptionalImage(imageKey, heightByWidth),
      createdAt = Some(now),
      updatedAt = Some(now)
    )
  }

  def getNewArticlePost(content: Option[String], imageKey: Option[String], heightByWidth: Option[Float]): Article = {
    val now = DateTime.now().toString
    Article(
      id = UuidUtils.getUUID,
      articleType = Some(ARTICLE_TYPE_POST),
      content = content.map(f => StringUtils.getTrimmedContent(f)),
      image = Image.getOptionalImage(imageKey, heightByWidth),
      createdAt = Some(now),
      updatedAt = Some(now)
    )
  }

  val ARTICLE_TYPE_POST = "post"
  val ARTICLE_TYPE_LINK = "link"
  val ARTICLE_TYPE_BLOG = "blog"

}
