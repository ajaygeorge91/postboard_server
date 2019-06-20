package models.daos

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import models._
import play.api.libs.json._
import play.api.libs.ws._
import utils.Constants

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class UserDAOImpl @Inject()(neo: Neo4J) extends UserDAO {

  /**
    * Finds a user by its login info.
    *
    * @param loginInfo The login info of the user to find.
    * @return The found user or None if no user for the given login info could be found.
    */
  def find(loginInfo: LoginInfo): Future[Option[User]] = {
    neo.cypher(
      """
        | MATCH (u:User)-[:HAS_LOGIN_INFO]->(l)
        | WHERE l.providerID = {providerID} AND
        |       l.providerKey = {providerKey}
        | RETURN u,l
      """.stripMargin, Json.obj(
        "providerID" -> JsString(loginInfo.providerID),
        "providerKey" -> JsString(loginInfo.providerKey)
      )).map(parseNeoUser)
  }

  /**
    * Finds a user by its user ID.
    *
    * @param userID The ID of the user to find.
    * @return The found user or None if no user for the given ID could be found.
    */
  def find(userID: String): Future[Option[User]] = {
    neo.cypher(
      """
        | MATCH (u:User)
        | WHERE u.id = {userID}
        | RETURN u
      """.stripMargin, Json.obj(
        "userID" -> userID.toString
      )).map(parseNeoUser)
  }

  /**
    * Finds a userProfile by its user ID.
    *
    * @param userID The ID of the user to find.
    * @return The found user or None if no user for the given ID could be found.
    */
  def findPrivateUserProfile(userID: String): Future[Option[User]] = {
    neo.cypher(
      """
        | MATCH (u:User)-[:HAS_LOGIN_INFO]->(l)
        | WHERE u.id = {userID}
        | OPTIONAL MATCH (u)-[:HAS_USER_PROFILE]->(p:UserProfile)
        | OPTIONAL MATCH (u)-[:HAS_LOGIN_INFO]->(l)
        | with u, p, {
        |    email:p.email,
        |    loginInfos:collect(l),
        |    rssUrl:p.rssUrl
        | } as userProfile
        | RETURN u, userProfile, p.avatarList
      """.stripMargin, Json.obj(
        "userID" -> userID.toString
      )).map(parseNeoUserProfile)
  }

  def getRssFeedUsers: Future[List[User]] = {
    neo.cypher(
      """
        | MATCH (u:User)-[:HAS_USER_PROFILE]->(p:UserProfile)
        | WHERE u.userType = {userType}
        | OPTIONAL MATCH (u)-[:HAS_LOGIN_INFO]->(l)
        | with u, p, {
        |    email:p.email,
        |    loginInfos:collect(l),
        |    rssUrl:p.rssUrl
        | } as userProfile
        | RETURN u, userProfile, p.avatarList
      """.stripMargin, Json.obj(
        "userType" -> Constants.USER_TYPE_RSS_FEED
      )).map(parseNeoUserProfileList)
  }

  def getUserFromRssUrl(rssUrl: String): Future[Option[User]] = {
    neo.cypher(
      """
        | MATCH (u:User)-[:HAS_USER_PROFILE]->(p:UserProfile)
        | WHERE p.rssUrl = {rssUrl}
        | OPTIONAL MATCH (u)-[:HAS_LOGIN_INFO]->(l)
        | with u, p, {
        |    email:p.email,
        |    loginInfos:collect(l),
        |    rssUrl:p.rssUrl
        | } as userProfile
        | RETURN u, userProfile, p.avatarList
      """.stripMargin, Json.obj(
        "rssUrl" -> rssUrl
      )).map(parseNeoUserProfile)
  }

  /**
    * Saves a user.
    *
    * @param user The user to save.
    * @return The saved user.
    */
  def save(user: User): Future[User] = {

    val jsonUser = Json.toJson(user).as[JsObject] - "loginInfo" - "userProfile"
    val jsonUserProfile = Json.toJson(user.userProfile.getOrElse(UserProfile())).as[JsObject] - "avatars"
    val avatarsList = UserProfile.getAvatarsAsStringList(user.userProfile.getOrElse(UserProfile()).avatars.getOrElse(List.empty))
    val jsonAvatars = Json.toJson(avatarsList).as[JsArray]
    val jsonLoginInfo = Json.toJson(user.loginInfo.get).as[JsObject] // throw error if login info not present

    neo.cypher(
      """
        | CREATE (n:User {props})
        | SET n.timestamp = timestamp()
        | CREATE (n)-[:HAS_LOGIN_INFO]->(l:LoginInfo {loginProps})
        | CREATE (n)-[:HAS_USER_PROFILE]->(p:UserProfile {userProfile})
        | SET p.avatarList = {avatars}
        | RETURN n,l
      """.stripMargin,
      Json.obj(
        "props" -> jsonUser,
        "loginProps" -> jsonLoginInfo,
        "userProfile" -> jsonUserProfile,
        "avatars" -> jsonAvatars
      )).map(parseNeoUser) map {
      case Some(u) => u
      case None => throw new Exception("error creating user")
    }
  }

  /**
    * Parses a WsResponse to get a unique user out of it.
    *
    * @param response response object
    * @return The parsed user.
    */
  def parseNeoUser(response: WSResponse): Option[User] = {
    (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (0) match {
      case _: JsUndefined => None
      case user =>
        val loginInfo = (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (1).asOpt[LoginInfo]
        val userobj = user.as[User]
        Some(userobj.copy(loginInfo = loginInfo))
    }
  }

  // todo make changes  in parseNeoUserProfileList as well
  def parseNeoUserProfile(response: WSResponse): Option[User] = {
    (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (0) match {
      case _: JsUndefined => None
      case user =>
        val userobj = user.as[User]
        val userProfile = (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (1).asOpt[UserProfile]
        val avatarListString = (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (2).asOpt[List[String]]
        val avatarList = avatarListString.map(f => UserProfile.getAvatarsFromStringList(f))
        Some(userobj.copy(userProfile = userProfile.map(f => f.copy(avatars = avatarList))))
    }
  }

  // todo make changes on parseNeoUserProfile first and copy it here
  def parseNeoUserProfileList(response: WSResponse): List[User] = {

    val jsObj = ((Json.parse(response.body) \ "results") (0) \ "data").as[List[JsObject]]
    val userlist = jsObj map { ob =>
      val userobj = (ob \ "row") (0).as[User]
      val userProfile = (ob \ "row") (1).asOpt[UserProfile]
      val avatarListString = (ob \ "row") (2).asOpt[List[String]]
      val avatarList = avatarListString.map(f => UserProfile.getAvatarsFromStringList(f))
      userobj.copy(userProfile = userProfile.map(f => f.copy(avatars = avatarList)))
    }
    userlist
  }


}
