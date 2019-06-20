package models.daos

import javax.inject.Inject

import models.{Article, ArticleMeta, Image, User}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws._
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class ArticleDAOImpl @Inject()(neo: Neo4J, config: Configuration) extends ArticleDAO {

  override def create(article: Article, user: User): Future[Option[Article]] = {

    val jsonArticle = Json.toJson(article).as[JsObject] - "image"
    val imageString = article.image.map(f => Image.getString(f)).getOrElse("")

    neo.cypher(
      """
        | MATCH (u:User)
        | WHERE u.id = {userId}
        | CREATE (u)-[:HAS_ARTICLE]->(a:Article {articleProps})
        | SET a.imageString = {imageString}
        | SET a.timestamp = timestamp()
        | RETURN a
      """.stripMargin,
      Json.obj(
        "articleProps" -> jsonArticle,
        "imageString" -> imageString,
        "userId" -> user.id.toString
      )).map(parseNeoArticle)
  }

  override def getNodeType(nodeID: String): Future[List[String]] = {
    neo.cypher(
      """
        |match (n{id:{nodeID}})
        |where n:Article or n:Comment
        |return labels(n)[0]
      """.stripMargin, Json.obj(
        "nodeID" -> nodeID
      )).map(r => parseStringList(r))
  }

  override def getArticlesConnectedToUser(userID: String, pageNumber: Int, numberOfRecords: Int): Future[List[Article]] = {
    neo.cypher(
      """
        |MATCH (a:Article)<-[r_a:HAS_ARTICLE]-(createdBy:User)
        | WHERE createdBy.id = {userID}
        | WITH  a, createdBy
        | OPTIONAL MATCH (a)-[r_c:HAS_COMMENT]->(c:Comment)
        | WITH a, createdBy, COUNT(DISTINCT(c)) as commentCount
        | OPTIONAL MATCH (a)<-[reaction:HAS_REACTION]-(u2)
        | RETURN a, createdBy, SUM(reaction.rating) as ratingSum, commentCount
        | ORDER BY (a.timestamp) DESC
        | SKIP {skipCount}
        | LIMIT {takeCount}
      """.stripMargin, Json.obj(
        "userID" -> userID,
        "skipCount" -> pageNumber * numberOfRecords,
        "takeCount" -> numberOfRecords
      )).map(r => parseNeoArticleList(r))
  }

  override def getArticleUserID(articleID: String): Future[Option[String]] = {
    neo.cypher(
      """
        |MATCH (a:Article{id:{articleID}})<-[r_a:HAS_ARTICLE]-(createdBy:User)
        | RETURN createdBy.id
      """.stripMargin, Json.obj(
        "articleID" -> articleID
      )).map(r => parseNeoString(r))
  }

  override def getArticlePublicDetails(articleId: String): Future[Option[Article]] = {
    neo.cypher(
      """
        | MATCH (a:Article)<-[r_a:HAS_ARTICLE]-(createdBy:User)
        | WHERE a.id = {articleId}
        | WITH  a, createdBy
        | OPTIONAL MATCH (a)-[r_c:HAS_COMMENT]->(c:Comment)
        | WITH a, createdBy, COUNT(DISTINCT(c)) as commentCount
        | OPTIONAL MATCH (a)<-[reaction:HAS_REACTION]-(u2)
        | RETURN a, createdBy, SUM(reaction.rating) as ratingSum, commentCount
      """.stripMargin, Json.obj(
        "articleId" -> articleId
      )).map(r => parseNeoArticle(r))
  }

  override def getArticleFromLink(link: String, pageNumber: Int, numberOfRecords: Int): Future[List[Article]] = {
    neo.cypher(
      """
        |MATCH (a:Article)
        | WHERE a.link CONTAINS {link}
        | RETURN a
        | ORDER BY (a.timestamp) DESC
        | SKIP {skipCount}
        | LIMIT {takeCount}
      """.stripMargin, Json.obj(
        "link" -> link,
        "skipCount" -> pageNumber * numberOfRecords,
        "takeCount" -> numberOfRecords
      )).map(r => parseNeoArticleList(r))
  }

  override def getNewArticles(pageNumber: Int, numberOfRecords: Int): Future[List[Article]] = {
    neo.cypher(
      """
        |MATCH (a:Article)<-[r_a:HAS_ARTICLE]-(createdBy:User)
        |WITH a, createdBy
        |ORDER BY (a.timestamp) DESC
        |SKIP {skipCount}
        |LIMIT {takeCount}
        |OPTIONAL MATCH (a)<-[reaction:HAS_REACTION]-(u2)
        |WITH a, createdBy, size((a)-[:HAS_COMMENT]->()) as commentCount, SUM(reaction.rating) as ratingSum
        |RETURN  a, createdBy, ratingSum, commentCount
      """.stripMargin, Json.obj(
        "skipCount" -> pageNumber * numberOfRecords,
        "takeCount" -> numberOfRecords
      )).map(r => parseNeoArticleList(r))
  }

  override def getHotArticles(pageNumber: Int, numberOfRecords: Int): Future[List[Article]] = {
    neo.cypher(
      """
        |MATCH (a:Article)<-[r_a:HAS_ARTICLE]-(createdBy:User)
        | WITH a, createdBy, size((a)-[:HAS_COMMENT]->()) as commentCount
        | OPTIONAL MATCH (a)<-[reaction:HAS_REACTION]-(u2)
        | WITH a, createdBy, SUM(reaction.rating) as ratingSum, commentCount
        | WITH a, createdBy, ratingSum, commentCount, (ratingSum + (commentCount/4)) as voteSum
        | WITH a, createdBy, ratingSum, commentCount, voteSum, case when voteSum =0 then 1 when voteSum < 1 then 1.0/abs(voteSum) else voteSum end as aggregate
        | RETURN  a, createdBy, ratingSum, commentCount, voteSum, aggregate, (aggregate / (((timestamp()-a.timestamp)/3600000)+2)^1.8) as rank
        | ORDER BY (rank) DESC
        | SKIP {skipCount}
        | LIMIT {takeCount}
      """.stripMargin, Json.obj(
        "skipCount" -> pageNumber * numberOfRecords,
        "takeCount" -> numberOfRecords
      )).map(r => parseNeoArticleList(r))
  }

  def parseNeoArticleList(response: WSResponse): List[Article] = {
    Logger.logger.debug(response.toString)
    val jsObj = ((Json.parse(response.body) \ "results") (0) \ "data").as[List[JsObject]]
    val article = jsObj map { ob =>
      val a = (ob \ "row") (0).as[Article]
      val user = (ob \ "row") (1).asOpt[User]
      val ratingSum = (ob \ "row") (2).asOpt[Long]
      val commentCount = (ob \ "row") (3).asOpt[Long]
      val rank = (ob \ "row") (6).asOpt[Double]
      val articleMetaOpt = Some(ArticleMeta(
        ratingSum = ratingSum,
        commentCount = commentCount,
        rank = rank))
      a.copy(articleMeta = articleMetaOpt,
        createdBy = user,
        image = Image.getFromString(a.imageString.getOrElse(""), config)
      )
    }
    article
  }

  def parseStringList(response: WSResponse): List[String] = {
    Logger.logger.debug(response.toString)
    val jsObj = ((Json.parse(response.body) \ "results") (0) \ "data").as[List[JsObject]]
    val listString = jsObj map { ob =>
      (ob \ "row") (0).as[String]
    }
    listString
  }

  def parseNeoArticle(response: WSResponse): Option[Article] = {
    (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (0).asOpt[Article] map { a =>
      val user = (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (1).asOpt[User]
      val ratingSum = (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (2).asOpt[Long]
      val commentCount = (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (3).asOpt[Long]
      val articleMetaOpt = Some(ArticleMeta(ratingSum = ratingSum, commentCount = commentCount))
      a.copy(articleMeta = articleMetaOpt,
        createdBy = user,
        image = Image.getFromString(a.imageString.getOrElse(""), config)
      )
    }
  }

  def parseNeoString(response: WSResponse): Option[String] = {
    (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (0).asOpt[String]
  }

}