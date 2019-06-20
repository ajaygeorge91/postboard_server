package models.daos

import javax.inject.Inject

import models._
import play.api.{Configuration, Logger}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by ajayg on 12/20/2016.
  */
class SearchDAOImpl @Inject()(neo: Neo4J, config: Configuration) extends SearchDAO {


  def getArticlesFromQuery(userID: String, searchKey: String, pageNumber: Int, numberOfRecords: Int): Future[List[Article]] = {
    neo.cypher(
      """
        | MATCH (n:Article)  WHERE n.title =~ {searchKey}  or  n.content =~ {searchKey}
        | OPTIONAL MATCH (n:Article)-[:HAS_ARTICLE]-(u:User)
        | RETURN n,u
        | SKIP {skipCount}
        | LIMIT {takeCount}
      """.stripMargin, Json.obj(
        "userID" -> userID,
        "searchKey" -> "(?i).*".concat(searchKey.concat(".*")),
        "skipCount" -> pageNumber * numberOfRecords,
        "takeCount" -> numberOfRecords
      )).map(r => parseNeoArticleList(r))
  }

  def getUsersFromQuery(userID: String, searchKey: String, pageNumber: Int, numberOfRecords: Int): Future[List[User]] = {
    neo.cypher(
      """
        | MATCH (n:User) WHERE n.fullName =~ {searchKey}
        | RETURN n
        | SKIP {skipCount}
        | LIMIT {takeCount}
      """.stripMargin, Json.obj(
        "userID" -> userID,
        "searchKey" -> "(?i).*".concat(searchKey.concat(".*")),
        "skipCount" -> pageNumber * numberOfRecords,
        "takeCount" -> numberOfRecords
      )).map(r => parseNeoUserList(r))
  }

  def parseNeoArticleList(response: WSResponse): List[Article] = {
    Logger.logger.debug(response.toString)
    val jsObj = ((Json.parse(response.body) \ "results") (0) \ "data").as[List[JsObject]]
    val article = jsObj map { ob =>
      val a = (ob \ "row") (0).as[Article]
      val user = (ob \ "row") (1).asOpt[User]
      a.copy(
        createdBy = user,
        image = Image.getFromString(a.imageString.getOrElse(""), config)
      )
    }
    article
  }


  def parseNeoUserList(response: WSResponse): List[User] = {

    val jsObj = ((Json.parse(response.body) \ "results") (0) \ "data").as[List[JsObject]]
    val userlist = jsObj map { ob =>
      val userobj = (ob \ "row") (0).as[User]
      userobj
    }
    userlist
  }



}
