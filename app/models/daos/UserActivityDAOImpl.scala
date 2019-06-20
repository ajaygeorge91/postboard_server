package models.daos

import javax.inject.Inject

import models.{UserActivity, UserCommentActivity, UserArticleActivity}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by ajayg on 12/20/2016.
  */
class UserActivityDAOImpl @Inject()(neo: Neo4J) extends UserActivityDAO {


  override def getUserActivities(userID: String, pageNumber: Int, numberOfRecords: Int): Future[List[UserActivity]] = {
    neo.cypher(
      """
        |MATCH (u:User)-[r:HAS_ARTICLE | COMMENTED_BY | HAS_REACTION]-(x)
        |WHERE u.id = {userID} AND (x:Article OR x:Comment)
        |WITH { relationshipLabel: TYPE(r), userActivity: r } as activity, x, labels(x)[0] as nodeLabel
        |WITH {
        |	nodeLabel: nodeLabel,
        |	article: case when nodeLabel = 'Article' then x else null end,
        |	comment: case when nodeLabel = 'Comment' then x else null end,
        |	userActivityList: collect(activity)
        |} as userActivity
        |RETURN userActivity
        |ORDER BY userActivity.a.timestamp DESC
        |SKIP {skipCount}
        |LIMIT {takeCount}
      """.stripMargin, Json.obj(
        "userID" -> userID,
        "skipCount" -> pageNumber * numberOfRecords,
        "takeCount" -> numberOfRecords
      )).map(r => parseNeoUserArticleActivities(r))
  }

  def parseNeoUserArticleActivities(response: WSResponse): List[UserActivity] = {
    val a = ((Json.parse(response.body) \ "results") (0) \ "data").as[List[JsObject]]
    val b = a map { ob =>
      (ob \ "row") (0).as[UserActivity]
    }
    b
  }

}
