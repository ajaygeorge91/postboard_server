package models.services.link_preview

import scala.xml.XML


/**
  * Created by ajayg on 10/12/2017.
  */
object RssLinkCrawler {

  def getLinks(url: String): List[String] = {
    val xml = XML.load(url)

    val itemList = (xml \\ "item").map(item => {
      (item \ "link").text
    })
    if (itemList.nonEmpty)
      return itemList.toList

    val entryList = (xml \\ "entry").map(entry => {
      (entry \\ "link" \ "@href").toString()
    })
    entryList.toList
  }

  def getRssItem(url: String): List[RssItem] = {
    val xml = XML.load(url)

    val itemList = (xml \\ "item").map(item => {
      RssItem(
        link = (item \ "link").text,
        title = Some((item \ "title").text.replaceAll("\\<.*?>", "")),
        description = Some((item \ "description").text.replaceAll("\\<.*?>", "")),
        image = Some((item \ "image").text.replaceAll("\\<.*?>", ""))
      )

    })
    if (itemList.nonEmpty)
      return itemList.toList

    val entryList = (xml \\ "entry").map(entry => {
      RssItem(
        link = (entry \\ "link" \ "@href").toString(),
        title = Some((entry \\ "title").text.replaceAll("\\<.*?>", "")),
        description = Some((entry \\ "summary").text.replaceAll("\\<.*?>", "")),
        image = Some((entry \\ "image").text.replaceAll("\\<.*?>", ""))
      )
    })
    entryList.toList
  }

}

case class RssItem(
                    link: String,
                    title: Option[String],
                    description: Option[String],
                    image: Option[String]
                  )
