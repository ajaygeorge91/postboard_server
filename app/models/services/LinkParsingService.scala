package models.services

import java.net.URL
import javax.imageio.ImageIO

import models.{Article, Image}
import models.services.link_preview.{RssLinkCrawler, TextCrawler}
import play.api.Logger

import scala.concurrent.Future

/**
  * Created by Android SD-1 on 27-03-2017.
  */
class LinkParsingService {

  def getArticleFromUrl(url: String): Future[Option[Article]] = {

    try {
      val sourceContent = TextCrawler.scrape(url, TextCrawler.ALL)
      val images = sourceContent.getImages
      val imgOpt = if (images.size() > 0) {

        val bi = ImageIO.read(new URL(images.get(0)))
        val heightByWidth = bi.getHeight.toFloat / bi.getWidth.toFloat * 100
        Some(Image(
          imageKey = Some(images.get(0)),
          heightByWidth = Some(heightByWidth)
        ))
      } else None
      if (sourceContent.getDescription.isEmpty && sourceContent.getTitle.isEmpty) {
        Future.successful(None)
      }
      Future.successful(Some(Article(id = "",
        articleType = Some(Article.ARTICLE_TYPE_LINK),
        title = Some(sourceContent.getTitle),
        content = Some(sourceContent.getDescription),
        link = Some(sourceContent.getFinalUrl),
        image = imgOpt
      )))
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        Future.successful(None)
    }

  }

}

