package utils

import org.joda.time.DateTime

/**
  * Created by ajayg on 12/21/2016.
  */
object WebUtils {


  def getDisplayTimeOffset(time: Option[String]): String = {
    time match {
      case Some(t) if t.nonEmpty =>
        try {
          val diff = (DateTime.now().getMillis - DateTime.parse(t).getMillis) / 60000 // minutes
          if (diff < 10) {
            "few moments ago"
          } else if (diff < 60) {
            diff + " minutes ago"
          } else if (diff < 1440) {
            val hour = diff / 60
            if (hour == 1)
              "1 hour ago"
            else
              hour + " hours ago"
          } else {
            val hour = (diff / 60) / 24
            if (hour == 1)
              "1 day ago"
            else
              hour + " days ago"

          }
        } catch {
          case e: Exception => e.printStackTrace()
            ""
        }
      case _ =>
        ""
    }
  }
}
