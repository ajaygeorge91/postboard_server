package models.daos

import java.util.NoSuchElementException
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.OAuth1Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import org.anormcypher.{Cypher, Neo4jREST}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.ws.WSResponse

import scala.collection.mutable
import scala.concurrent.Future

/**
  * The DAO to store the password information.
  */
class PasswordInfoDAO @Inject()(neo: Neo4J) extends DelegableAuthInfoDAO[PasswordInfo] {


  implicit val PasswordInfoWrites: Writes[PasswordInfo] = (
    (JsPath \ "hasher").write[String] and
      (JsPath \ "password").write[String] and
      (JsPath \ "salt").writeNullable[String]
    ) (unlift(PasswordInfo.unapply))

  implicit val OAuth1InfoReads: Reads[PasswordInfo] = (
    (JsPath \ "hasher").read[String] and
      (JsPath \ "password").read[String] and
      (JsPath \ "salt").readNullable[String]
    ) (PasswordInfo)

  /**
    * Finds the auth info which is linked with the specified login info.
    *
    * @param loginInfo The linked login info.
    * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
    */
  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    neo.cypher(
      """
        | MATCH (l:LoginInfo)-[:HAS_PASSWORD_INFO]->(p)
        | WHERE l.providerID = {providerID} AND
        |       l.providerKey = {providerKey}
        | RETURN p
      """.stripMargin, Json.obj(
        "providerID" -> JsString(loginInfo.providerID),
        "providerKey" -> JsString(loginInfo.providerKey)
      )).map(parseNeoPasswordInfo)
  }

  /**
    * Adds new auth info for the given login info.
    *
    * @param loginInfo    The login info for which the auth info should be added.
    * @param passwordInfo The passwordInfo to add.
    * @return The added passwordInfo.
    */
  def add(loginInfo: LoginInfo, passwordInfo: PasswordInfo): Future[PasswordInfo] = {
    neo.cypher(
      """
        | MATCH (l:LoginInfo)
        | WHERE l.providerID = {providerID} AND
        |       l.providerKey = {providerKey}
        | CREATE (l)-[:HAS_PASSWORD_INFO]->(p:PasswordInfo {props})
        | RETURN p
      """.stripMargin,
      Json.obj(
        "providerID" -> JsString(loginInfo.providerID),
        "providerKey" -> JsString(loginInfo.providerKey),
        "props" -> writeNeoPasswordInfo(passwordInfo)
      )
    ).map(res => passwordInfo)
  }

  /**
    * Updates the auth info for the given login info.
    *
    * @param loginInfo    The login info for which the auth info should be updated.
    * @param passwordInfo The passwordInfo to update.
    * @return The updated passwordInfo.
    */
  def update(loginInfo: LoginInfo, passwordInfo: PasswordInfo): Future[PasswordInfo] = {
    neo.cypher(
      """
        | MATCH (l:LoginInfo)-[:HAS_PASSWORD_INFO]->(p)
        | WHERE l.providerID = {providerID} AND
        |       l.providerKey = {providerKey}
        | SET p = {props}
        | RETURN p
      """.stripMargin,
      Json.obj(
        "providerID" -> JsString(loginInfo.providerID),
        "providerKey" -> JsString(loginInfo.providerKey),
        "props" -> writeNeoPasswordInfo(passwordInfo)
      )
    ).map(res => passwordInfo)
  }

  /**
    * Saves the auth info for the given login info.
    *
    * This method either adds the auth info if it doesn't exists or it updates the auth info
    * if it already exists.
    *
    * @param loginInfo The login info for which the auth info should be saved.
    * @param authInfo  The auth info to save.
    * @return The saved auth info.
    */
  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None => add(loginInfo, authInfo)
    }
  }

  /**
    * Removes the auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be removed.
    * @return A future to wait for the process to be completed.
    */
  def remove(loginInfo: LoginInfo): Future[Unit] = {
    neo.cypher(
      """
        | MATCH (l:LoginInfo)-[r:HAS_PASSWORD_INFO]->(p)
        | WHERE l.providerID = {providerID} AND
        |       l.providerKey = {providerKey}
        | DELETE l,p
      """.stripMargin,
      Json.obj(
        "providerID" -> JsString(loginInfo.providerID),
        "providerKey" -> JsString(loginInfo.providerKey)
      )
    ).map(res => Unit)
  }

  /**
    * Parses a WsResponse to get a unique PasswordInfo out of it.
    *
    * @param response response object
    * @return The parsed PasswordInfo.
    */
  def parseNeoPasswordInfo(response: WSResponse) = (((Json.parse(response.body) \ "results") (0) \ "data") (0) \ "row") (0).asOpt[PasswordInfo]

  def writeNeoPasswordInfo(passwordInfo: PasswordInfo) = {
    val jsonAuth = Json.toJson(passwordInfo)
    val params = (jsonAuth \ "params").asOpt[JsValue]

    params match {
      case None => jsonAuth
      case Some(p) => (jsonAuth.as[JsObject] - "params") ++ Json.obj("params" -> Json.stringify(p))
    }
  }
}