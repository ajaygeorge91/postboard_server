package models.services

import models._

import scala.concurrent.Future

/**
  * Created by ajayg on 10/5/2016.
  */
trait LinkParsingService {

  def getArticleFromUrl(url:String): Future[Option[Article]]

}
