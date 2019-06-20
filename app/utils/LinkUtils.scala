package utils

/**
  * Created by ajayg on 10/16/2017.
  */
object LinkUtils {

  val HTTP_PROTOCOL = "http://"
  val HTTPS_PROTOCOL = "https://"

  /**
    * Returns the cannoncial url
    */
  def cannonicalPage(origUrl: String): String = {

    val url: String = if (origUrl.startsWith(LinkUtils.HTTP_PROTOCOL))
      origUrl.substring(LinkUtils.HTTP_PROTOCOL.length)
    else if (origUrl.startsWith(LinkUtils.HTTPS_PROTOCOL))
      origUrl.substring(LinkUtils.HTTPS_PROTOCOL.length)
    else
      ""
    url.split("/").headOption.getOrElse("")

  }

  def getFavIco(origUrl: String): String = {
    val header: String = if (origUrl.startsWith(LinkUtils.HTTP_PROTOCOL))
      LinkUtils.HTTP_PROTOCOL
    else if (origUrl.startsWith(LinkUtils.HTTPS_PROTOCOL))
      LinkUtils.HTTPS_PROTOCOL
    else
      ""
    header + cannonicalPage(origUrl) + "/favicon.ico"
  }


}
