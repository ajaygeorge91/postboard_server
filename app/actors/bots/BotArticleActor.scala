package actors.bots

import java.util.concurrent.TimeUnit
import javax.inject.Named

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.google.inject.Inject
import models.services.{ArticleService, LinkParsingService}
import models.services.link_preview.{RssItem, TextCrawler}
import models.{Article, User, UserArticleActivity}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

/**
  * Created by Android SD-1 on 22-03-2017.
  */

case class CreateArticleFromRssItem(rssItem: RssItem, user: User)

case class CreateArticleFromLink(link: String, user: User)

case class CreateArticle(article: Article, user: User)

case class Create(user: User)

case class Read(user: User, page: Int)

case class ReadNew(user: User, page: Int)

class BotArticleActor @Inject()(articleService: ArticleService,
                                rssParsingService: LinkParsingService,
                                @Named(BotCommentActor.name) val botCommentActor: ActorRef)(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  def commentAction(user: User, articleID: String): Unit = {
    Random.nextInt(3) match {
      case 0 =>
        botCommentActor ! CreateComment(articleID, user)
      case 1 =>
        botCommentActor ! ReadNewComment(articleID, user, 0)
      case 2 =>
        botCommentActor ! ReadComment(articleID, user, 0)
    }
  }

  override def receive: Receive = {

    case CreateArticleFromRssItem(rssItem: RssItem, user: User) =>
      val finalLink = TextCrawler.unshortenUrl(rssItem.link)
      articleService.getArticlesFromLink(finalLink, 0, 10) map { aList =>
        if (aList.isEmpty) {
          rssParsingService.getArticleFromUrl(finalLink) map {
            case Some(artFromWeb) =>

              val artTitle = if (artFromWeb.title.getOrElse("").length > rssItem.title.getOrElse("").length) {
                artFromWeb.title.getOrElse("")
              } else {
                rssItem.title.getOrElse("")
              }

              val artDescription = if (artFromWeb.content.getOrElse("").length > rssItem.description.getOrElse("").length) {
                artFromWeb.content.getOrElse("")
              } else {
                rssItem.description.getOrElse("")
              }

              val art = artFromWeb.copy(title = Some(artTitle), content = Some(artDescription))

              self ! CreateArticle(art, user)

            case None =>
          }
        }
      }

    case CreateArticleFromLink(link: String, user: User) =>
      val finalLink = TextCrawler.unshortenUrl(link)
      articleService.getArticlesFromLink(finalLink, 0, 10) map { aList =>
        if (aList.isEmpty) {
          rssParsingService.getArticleFromUrl(finalLink) map {
            case Some(artFromWeb) =>
              if (artFromWeb.content.nonEmpty && artFromWeb.title.nonEmpty)
                self ! CreateArticle(artFromWeb, user)

            case None =>
          }
        }
      }

    case CreateArticle(article: Article, user: User) =>
      articleService.createArticle(article, user) map { a =>
        log.debug("article created " + a.id)
      }

    case Create(user: User) =>
      val (titleO, contentO) = if ((Integer.parseInt(user.id) % 2) == 0) {
        (Some(Contents.takeLoremRandom()), Some(Contents.takeLoremRandomContent()))
      } else {
        (Some(Contents.takeLoremRandomMal()), Some(Contents.takeLoremRandomContentMal()))
      }

      val (imageKeyO, heightByWidthO) = if (Random.nextBoolean()) {
        (Some("cb8126f3-858c-43f0-8d34-d4f92e8438d9"), Some(103.16743F))
      } else {
        (Some("04bbd208-920a-482f-bb9f-cd5b3a4e7fa7"), Some(45.42124557495117F))
      }

      val rand = Random.nextInt(13)
      val article =
        if (rand == 0 && rand == 6) {
          Article.getNewArticlePostSample(title = titleO, imageKey = imageKeyO, heightByWidth = heightByWidthO, content = contentO)
        } else if (rand == 1 && rand == 7) {
          Article.getNewArticlePostSample(title = titleO, imageKey = imageKeyO, heightByWidth = heightByWidthO, content = None)
        } else if (rand == 2 && rand == 8 && rand == 9) {
          Article.getNewArticlePostSample(title = titleO, imageKey = None, heightByWidth = None, content = contentO)
        } else if (rand == 3) {
          Article.getNewArticlePostSample(title = titleO, imageKey = None, heightByWidth = None, content = None)
        } else if (rand == 4 && rand == 10 && rand == 11 && rand == 12) {
          Article.getNewArticlePostSample(title = None, imageKey = imageKeyO, heightByWidth = heightByWidthO, content = contentO)
        } else if (rand == 5) {
          Article.getNewArticlePostSample(title = None, imageKey = None, heightByWidth = None, content = contentO)
        } else {
          Article.getNewArticlePostSample(title = None, imageKey = imageKeyO, heightByWidth = heightByWidthO, content = None)
        }
      articleService.createArticle(article, user) map { a =>
        log.error("Article created | " + a.id + " | " + a.title.getOrElse(""))
      }
    case ReadNew(user: User, page: Int) =>
      log.error("page : " + page)
      articleService.getArticlesWithUserAction(user.id, page, 10) map (l => l.foreach { a =>
        if (Random.nextInt(100) > 80) {
          val len = Random.nextInt(a.title.getOrElse(a.content.getOrElse(Contents.takeLoremRandom())).split(" ").length)
          val rating = if (len >= 4 && len <= 20) 1 else -1
          articleService.addArticleActivityFromUser(user.id, a.id, UserArticleActivity(rating = Some(rating))) map { act =>
            log.error("Article activity created | " + act.rating.getOrElse(0))
          }
        }
        if (Random.nextInt(100) > 70) {
          commentAction(user, a.id)
        }
      })
      if (Random.nextInt(100) > 70) {
        self ! ReadNew(user, page + 1)
      }
    case Read(user: User, page: Int) =>
      articleService.getHotArticlesWithUserAction(user.id, page, 10) map (l => l.foreach { a =>
        if (Random.nextInt(100) > 50) {
          val len = Random.nextInt(a.title.getOrElse(a.content.getOrElse(Contents.takeLoremRandom())).split(" ").length)
          val rating = if (len >= 4 && len <= 20) 1 else -1
          articleService.addArticleActivityFromUser(user.id, a.id, UserArticleActivity(rating = Some(rating))) map { act =>
            log.error("Article activity created | " + act.rating.getOrElse(0))
          }
        }
        if (Random.nextInt(100) > 40) {
          commentAction(user, a.id)
        }
      })
      if (Random.nextInt(100) > 40) {
        self ! Read(user, page + 1)
      }
  }
}

object BotArticleActor {
  final val name = "BotArticleActor"

}