package models.daos

import javax.inject.Inject

import models.{Article, UserArticleActivity}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSResponse

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by ajayg on 12/20/2016.
  */
class UserArticleActivityDAOImpl @Inject()(neo: Neo4J) extends UserArticleActivityDAO {

  override def getActivity(userId: String, articleId: String): Future[Option[UserArticleActivity]] = {
    neo.cypher(
      """
        | MATCH (a:Article)<-[r:HAS_REACTION]-(u:User)
        | WHERE a.id = {articleID} and u.id = {userId}
        | RETURN r
      """.stripMargin, Json.obj(
        "articleID" -> articleId,
        "userId" -> userId
      )).map(r => parseNeoUserArticleReaction(r))
  }

  override def addActivity(userID: String, articleID: String, userArticleActivity: UserArticleActivity): Future[Option[UserArticleActivity]] = {
    val jsonObject = Json.toJson(userArticleActivity).as[JsObject]

    neo.cypher(
      """
        | MATCH (u:User), (a:Article)
        | WHERE u.id = {userId} AND a.id = {articleID}
        | MERGE (u)-[r:HAS_REACTION]->(a)
        | ON CREATE SET r = {props}
        | ON MATCH SET r = {props}
        | SET r.timestamp = timestamp()
        | RETURN r
      """.stripMargin,
      Json.obj(
        "props" -> jsonObject,
        "userId" -> userID,
        "articleID" -> articleID
      )).map(parseNeoUserArticleReaction)
  }

  def parseNeoUserArticleReaction(response: WSResponse): Option[UserArticleActivity] = {
    (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (0).asOpt[UserArticleActivity]
  }

}
