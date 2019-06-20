package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
  * Created by ajayg on 12/28/2016.
  */
case class PaginatedResult[I](
                               result: List[I],
                               pageNumber: Int,
                               numberOfRecords: Int,
                               orderBy: String = "",
                               totalCount: Long = 0
                             )


case object PaginatedResult extends {
  implicit def pageFormat[I: Format]: Format[PaginatedResult[I]] =
    ((__ \ "result").format[List[I]] ~
      (__ \ "pageNumber").format[Int] ~
      (__ \ "numberOfRecords").format[Int] ~
      (__ \ "orderBy").format[String] ~
      (__ \ "totalCount").format[Long]) (PaginatedResult.apply, unlift(PaginatedResult.unapply))


  def get[L](pageNumber: Int, numberOfRecords: Int, orderBy: String, list: List[L]): PaginatedResult[L] = {
    PaginatedResult[L](pageNumber = pageNumber, numberOfRecords = numberOfRecords, orderBy = orderBy, result = list)
  }

}

