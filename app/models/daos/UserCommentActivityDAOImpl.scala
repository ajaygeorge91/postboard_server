package models.daos

import javax.inject.Inject

import models.{UserCommentActivity, UserArticleActivity}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by ajayg on 12/20/2016.
  */
class UserCommentActivityDAOImpl @Inject()(neo: Neo4J) extends UserCommentActivityDAO {

  override def addCommentOrSubCommentReaction(userID: String, commentID: String, userCommentActivity: UserCommentActivity): Future[Option[UserCommentActivity]] = {
    val jsonObject = Json.toJson(userCommentActivity).as[JsObject]

    neo.cypher(
      """
        | MATCH (u:User), (c)
        | WHERE u.id = {userId} AND c.id = {commentID} AND (c:Comment or c:SubComment)
        | MERGE (u)-[r:HAS_REACTION]->(c)
        | ON CREATE SET r = {props}
        | ON MATCH SET r = {props}
        | SET r.timestamp = timestamp()
        | RETURN r
      """.stripMargin,
      Json.obj(
        "props" -> jsonObject,
        "userId" -> userID,
        "commentID" -> commentID
      )).map(parseNeoUserCommentReaction)
  }

  def parseNeoUserCommentReaction(response: WSResponse): Option[UserCommentActivity] = {
    (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (0).asOpt[UserCommentActivity]
  }

}
