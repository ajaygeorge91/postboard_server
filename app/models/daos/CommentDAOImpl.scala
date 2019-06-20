package models.daos

import javax.inject.Inject

import models._
import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class CommentDAOImpl @Inject()(neo: Neo4J) extends CommentDAO {

  override def createComment(articleID: String, comment: Comment, user: User): Future[Option[Comment]] = {

    val jsonObj = Json.toJson(comment).as[JsObject]

    neo.cypher(
      """
        | MATCH (u:User)
        | WHERE u.id = {userID}
        | MATCH (a:Article)
        | WHERE a.id = {articleId}
        | WITH a, u
        | CREATE (a)-[:HAS_COMMENT]->(c:Comment {props})
        | SET c.timestamp = timestamp()
        | CREATE (u)<-[:COMMENTED_BY]-(c)
        | RETURN c,u
      """.stripMargin,
      Json.obj(
        "props" -> jsonObj,
        "articleId" -> articleID,
        "userID" -> user.id.toString
      )).map(parseNeoComment)
  }

  override def createSubComment(commentID: String, subComment: SubComment, user: User): Future[Option[SubComment]] = {

    val jsonObj = Json.toJson(subComment).as[JsObject]

    neo.cypher(
      """
        | MATCH (u:User)
        | WHERE u.id = {userID}
        | MATCH (c:Comment)
        | WHERE c.id = {commentID}
        | CREATE (c)-[:HAS_SUB_COMMENT]->(sc:SubComment {props})
        | SET sc.timestamp = timestamp()
        | CREATE (u)<-[:COMMENTED_BY]-(sc)
        | RETURN sc,u
      """.stripMargin,
      Json.obj(
        "props" -> jsonObj,
        "userID" -> user.id,
        "commentID" -> commentID
      )).map(parseNeoSubComment)
  }

  override def getAllCommentsForArticle(articleID: String, pageNumber: Int, numberOfRecords: Int): Future[List[Comment]] = {
    neo.cypher(
      """
        | MATCH (a:Article)<-[:HAS_COMMENT]->(c)
        | WHERE a.id = {articleID}
        | RETURN c
        | SKIP {skipCount}
        | LIMIT {takeCount}
      """.stripMargin, Json.obj(
        "articleID" -> articleID,
        "skipCount" -> pageNumber * numberOfRecords,
        "takeCount" -> numberOfRecords
      )).map(r => parseNeoCommentList(r))
  }

  override def getCommentUserID(commentID: String): Future[Option[String]] = {
    neo.cypher(
      """
        |MATCH (a:Comment{id:{commentID}})-[r_c:COMMENTED_BY]->(createdBy:User)
        | RETURN createdBy.id
      """.stripMargin, Json.obj(
        "commentID" -> commentID
      )).map(r => parseNeoString(r))
  }

  override def getCommentsDetails(commentID: String, withUserID: String): Future[Option[Comment]] = {
    neo.cypher(
      """
        |
        |MATCH (a:Article)-[r_hc:HAS_COMMENT]->(c:Comment)-[:COMMENTED_BY]->(u)
        | WHERE c.id = {commentID}
        |OPTIONAL MATCH (c)<-[r_hr_c:HAS_REACTION]-()
        |with c, r_hr_c, u, a, {
        |	ratingSum: sum(r_hr_c.rating),
        |  	value: ( ( size(filter(x IN collect(r_hr_c) WHERE x.rating = 1)) + size(filter(x IN collect(r_hr_c) WHERE x.rating = -1))/4  ) / ((((timestamp()-c.timestamp)/3600000)+2)^0.8) )
        |} as commentMetaT
        |OPTIONAL MATCH (u_current:User{id:{currentUserID}})-[r_hr_u_current_c:HAS_REACTION]->(c)
        |using index u_current:User(id)
        |with c, u, a,commentMetaT, {
        |	ratingSum: commentMetaT.ratingSum,
        |	value: commentMetaT.value,
        |	subCommentCount: size((c)-[:HAS_SUB_COMMENT]->())
        |} as commentMeta, {
        |	rating: r_hr_u_current_c.rating
        |} as userCommentActivity with {
        |	id: c.id,
        |	content: c.content,
        |    userCommentActivity:userCommentActivity,
        |    commentMeta: commentMeta,
        |    createdAt: c.createdAt,
        |    createdBy: u
        |} as comment, u, a
        |return comment, u, a
        |
      """.stripMargin, Json.obj(
        "commentID" -> commentID,
    "currentUserID" -> withUserID
      )).map(r => parseNeoComment(r))
  }

  override def getSubCommentsDetails(subCommentID: String): Future[Option[SubComment]] = {
    neo.cypher(
      """
        | MATCH (c:SubComment)-[:COMMENTED_BY]->(u)
        | WHERE c.id = {subCommentID}
        | RETURN c, u
      """.stripMargin, Json.obj(
        "subCommentID" -> subCommentID
      )).map(r => parseNeoSubComment(r))
  }

  override def editCommentContent(commentID: String, content: String): Future[Option[Comment]] = {
    neo.cypher(
      """
        | MATCH (c:Comment)
        | WHERE c.id = {commentID}
        | SET c.content = {content}
        | RETURN c
      """.stripMargin, Json.obj(
        "commentID" -> commentID,
        "content" -> content
      )).map(r => parseNeoComment(r))
  }

  override def editSubCommentContent(subCommentID: String, content: String): Future[Option[SubComment]] = {
    neo.cypher(
      """
        | MATCH (sc:SubComment)
        | WHERE sc.id = {subCommentID}
        | SET sc.content = {content}
        | RETURN sc
      """.stripMargin, Json.obj(
        "subCommentID" -> subCommentID,
        "content" -> content
      )).map(r => parseNeoSubComment(r))
  }

  override def getHotCommentsForArticleWithUserReaction(articleID: String, withUserID: String, pageNumber: Int, numberOfRecords: Int): Future[List[Comment]] = {
    neo.cypher(
      """
        |MATCH (a:Article{id:{articleID}})-[r_hc:HAS_COMMENT]->(c)
        |with  c
        |OPTIONAL MATCH (c)<-[r_hr_c:HAS_REACTION]-()
        |with c, r_hr_c
        |with c, {
        |	ratingSum: sum(r_hr_c.rating),
        |  	value: ( ( size(filter(x IN collect(r_hr_c) WHERE x.rating = 1)) + size(filter(x IN collect(r_hr_c) WHERE x.rating = -1))/4  ) / ((((timestamp()-c.timestamp)/3600000)+2)^0.8) )
        |} as commentMetaT
        |order by commentMetaT.value desc
        |SKIP {skipCount}
        |LIMIT {takeCount}
        |
        |MATCH (c)-[:COMMENTED_BY]->(u_c:User)
        |with c, u_c, commentMetaT
        |OPTIONAL MATCH (u_current:User{id:{currentUserID}})-[r_hr_u_current_c:HAS_REACTION]->(c)
        |using index u_current:User(id)
        |with c, u_c, size((c)-[:HAS_SUB_COMMENT]->()) as subCommentCount,  commentMetaT, {
        |	rating: r_hr_u_current_c.rating
        |} as userCommentActivity, {
        |	ratingSum: commentMetaT.ratingSum,
        |	value: commentMetaT.value,
        |	subCommentCount: size((c)-[:HAS_SUB_COMMENT]->())
        |} as commentMeta
        |with {
        |	id: c.id,
        |	content: c.content,
        |    commentMeta: commentMeta,
        |    userCommentActivity:userCommentActivity,
        |    createdAt: c.createdAt,
        |    createdBy: u_c
        |} as comment
        |return comment
      """.stripMargin, Json.obj(
        "articleID" -> articleID,
        "currentUserID" -> withUserID,
        "skipCount" -> pageNumber * numberOfRecords,
        "takeCount" -> numberOfRecords
      )).map(r => parseNeoCommentList(r))
  }

  override def getNewCommentsForArticleWithUserReaction(articleID: String, withUserID: String, pageNumber: Int, numberOfRecords: Int): Future[List[Comment]] = {
    neo.cypher(
      """
        |MATCH (a:Article{id:{articleID}})-[r_hc:HAS_COMMENT]->(c)
        |with  c
        |OPTIONAL MATCH (c)<-[r_hr_c:HAS_REACTION]-()
        |with c, r_hr_c
        |with c
        |order by c.timestamp desc
        |SKIP {skipCount}
        |LIMIT {takeCount}
        |
        |MATCH (c)-[:COMMENTED_BY]->(u_c:User)
        |with c, u_c
        |OPTIONAL MATCH (u_current:User{id:{currentUserID}})-[r_hr_u_current_c:HAS_REACTION]->(c)
        |using index u_current:User(id)
        |with c, u_c, size((c)-[:HAS_SUB_COMMENT]->()) as subCommentCount,  {
        |	rating: r_hr_u_current_c.rating
        |} as userCommentActivity, {
        |	subCommentCount: size((c)-[:HAS_SUB_COMMENT]->())
        |} as commentMeta
        |with {
        |	id: c.id,
        |	content: c.content,
        |    commentMeta: commentMeta,
        |    userCommentActivity:userCommentActivity,
        |    createdAt: c.createdAt,
        |    createdBy: u_c
        |} as comment
        |return comment
        |
      """.stripMargin, Json.obj(
        "articleID" -> articleID,
        "currentUserID" -> withUserID,
        "skipCount" -> pageNumber * numberOfRecords,
        "takeCount" -> numberOfRecords
      )).map(r => parseNeoCommentList(r))
  }


  override def getHotSubCommentsForCommentWithUserReaction(commentID: String, withUserID: String, pageNumber: Int, numberOfRecords: Int): Future[List[SubComment]] = {
    neo.cypher(
      """
        |MATCH  (c:Comment{id:{commentID}})-[r_hsc:HAS_SUB_COMMENT]->(sc:SubComment)
        |OPTIONAL MATCH (sc)<-[r_hr_sc:HAS_REACTION]-(u2)
        |with  sc, {
        |   ratingSum: sum(r_hr_sc.rating),
        |   value: ( ( size(filter(x IN collect(r_hr_sc) WHERE x.rating = 1)) + size(filter(x IN collect(r_hr_sc) WHERE x.rating = -1))/4 ) / ((((timestamp()-sc.timestamp)/3600000)+2)^0.8) )
        |} as subCommentMeta
        |
        |order by subCommentMeta.value desc
        |SKIP {skipCount}
        |LIMIT {takeCount}
        |
        |MATCH (sc)-[:COMMENTED_BY]->(u_sc:User)
        |with sc, u_sc, subCommentMeta
        |OPTIONAL MATCH (u_current:User{id:{currentUserID}})-[r_hr_u_current_sc:HAS_REACTION]->(sc)
        |with sc, u_sc, subCommentMeta, {
        |	rating: r_hr_u_current_sc.rating
        |} as userSubCommentActivity
        |return   CASE WHEN sc IS NOT NULL THEN {
        |	  id: sc.id,
        |	  content: sc.content,
        |	  subCommentMeta: subCommentMeta,
        |	  userSubCommentActivity:userSubCommentActivity,
        |	  createdAt: sc.createdAt,
        |	  createdBy: u_sc
        |} ELSE NULL END as subCommentList
      """.stripMargin, Json.obj(
        "commentID" -> commentID,
        "currentUserID" -> withUserID,
        "skipCount" -> pageNumber * numberOfRecords,
        "takeCount" -> numberOfRecords
      )).map(r => parseNeoSubCommentList(r))
  }

  override def getNewSubCommentsForCommentWithUserReaction(commentID: String, withUserID: String, pageNumber: Int, numberOfRecords: Int): Future[List[SubComment]] = {
    neo.cypher(
      """
        |MATCH  (c:Comment{id:{commentID}})-[r_hsc:HAS_SUB_COMMENT]->(sc:SubComment)
        |WITH sc
        |order by sc.timestamp desc
        |SKIP {skipCount}
        |LIMIT {takeCount}
        |
        |MATCH (sc)-[:COMMENTED_BY]->(u_sc:User)
        |with sc, u_sc
        |OPTIONAL MATCH (sc)<-[r_hr_sc:HAS_REACTION]-(u2)
        |with  sc, u_sc, {
        |   ratingSum: sum(r_hr_sc.rating)
        |} as subCommentMeta
        |OPTIONAL MATCH (u_current:User{id:{currentUserID}})-[r_hr_u_current_sc:HAS_REACTION]->(sc)
        |with sc, u_sc, subCommentMeta, {
        |	rating: r_hr_u_current_sc.rating
        |} as userSubCommentActivity
        |return   CASE WHEN sc IS NOT NULL THEN {
        |	  id: sc.id,
        |	  content: sc.content,
        |	  subCommentMeta: subCommentMeta,
        |	  userSubCommentActivity:userSubCommentActivity,
        |	  createdAt: sc.createdAt,
        |	  createdBy: u_sc
        |} ELSE NULL END as subCommentList
      """.stripMargin, Json.obj(
        "commentID" -> commentID,
        "currentUserID" -> withUserID,
        "skipCount" -> pageNumber * numberOfRecords,
        "takeCount" -> numberOfRecords
      )).map(r => parseNeoSubCommentList(r))
  }


  def parseNeoCommentList(response: WSResponse): List[Comment] = {
    Logger.logger.debug(response.toString)
    val jsList = ((Json.parse(response.body) \ "results") (0) \ "data").as[List[JsObject]]
    val b = jsList map { ob =>
      val cmt = (ob \ "row") (0).as[Comment]
      cmt
    }
    b
  }

  def parseNeoSubCommentList(response: WSResponse): List[SubComment] = {
    Logger.logger.debug(response.toString)
    val jsList = ((Json.parse(response.body) \ "results") (0) \ "data").as[List[JsObject]]
    val b = jsList map { ob =>
      val cmt = (ob \ "row") (0).as[SubComment]
      cmt
    }
    b
  }


  def parseNeoComment(response: WSResponse): Option[Comment] = {
    (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (0).asOpt[Comment] map { c =>
      val user = (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (1).asOpt[User]
      val article = (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (2).asOpt[Article]
      c.copy(createdBy = c.createdBy.map(u => Some(u)).getOrElse(user), articleID = article.map(a => a.id))
    }
  }

  def parseNeoSubComment(response: WSResponse): Option[SubComment] = {
    (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (0).asOpt[SubComment] map { c =>
      val user = (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (1).asOpt[User]
      c.copy(createdBy = user)
    }
  }

  def parseNeoString(response: WSResponse): Option[String] = {
    (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (0).asOpt[String]
  }

}