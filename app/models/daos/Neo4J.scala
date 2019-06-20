package models.daos

import java.util.concurrent.TimeUnit
import javax.inject.Inject

import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
  * Created by ajayg on 9/9/2016.
  */
class Neo4J @Inject()(
                       implicit val config: Configuration,
                       ws: WSClient) {

  val NEO4J_ENDPOINT =
    config.getString("neo4j.server").getOrElse("http://localhost") + ":" +
      config.getInt("neo4j.port").getOrElse("7474") +
      config.getString("neo4j.endpoint").getOrElse("/db/data/")

  val NEO4J_USER = config.getString("neo4j.username").getOrElse("neo4j")
  val NEO4J_PASSWORD = config.getString("neo4j.password").getOrElse("neo4j")

  ws.url(NEO4J_ENDPOINT)
    .withAuth(NEO4J_USER, NEO4J_PASSWORD, WSAuthScheme.BASIC)
    .withHeaders("Accept" -> "application/json ; charset=UTF-8", "Content-Type" -> "application/json")
    .withRequestTimeout(Duration(10, TimeUnit.SECONDS))
    .get()
    .map(res => {
      res.status match {
        case 200 =>
          val json = Json.parse(res.body)
          if ((json \ "errors").as[Seq[JsObject]].nonEmpty) {
            throw new Exception(res.body)
          }
        case _ => throw new Exception("Could not Connect to the Neo4j Database")
      }
    })

  /**
    * Sends a Cypher query to the neo4j server
    *
    * @param query      Cypher query sent to the server
    * @param parameters parameter object to be used by the query. (See Cypher reference for more details)
    * @return
    */
  def cypher(query: String, parameters: JsObject): Future[WSResponse] = {
    val request: WSRequest = ws.url(NEO4J_ENDPOINT + "transaction/commit")

    buildNeo4JRequest(request).post(Json.obj(
      "statements" -> Json.arr(
        Json.obj(
          "statement" -> query,
          "parameters" -> parameters
        )
      )
    )).map(response => {
      response.status match {
        case 200 =>
          val json = Json.parse(response.body)
          if ((json \ "errors").as[Seq[JsObject]].nonEmpty) {
            throw new Exception(response.body)
          }
          response
        case _ => throw new Exception(response.body)
      }
    })
  }

  /**
    * Builds a request to be sent to the neo4J database
    *
    * @param req request to be modified
    * @return modified request
    */
  def buildNeo4JRequest(req: WSRequest): WSRequest = req
    .withAuth(NEO4J_USER, NEO4J_PASSWORD, WSAuthScheme.BASIC)
    .withHeaders("Accept" -> "application/json ; charset=UTF-8", "Content-Type" -> "application/json")
    .withRequestTimeout(Duration(20, TimeUnit.SECONDS))
}