package actors.bots

import java.util.concurrent.TimeUnit
import javax.inject.Named

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem}
import com.google.inject.Inject
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasher
import models.UserProfile
import models.services.link_preview.RssLinkCrawler
import models.services.{ArticleService, UserService}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

/**
  * Created by Android SD-1 on 22-03-2017.
  */

class BotRssCrawlerActor @Inject()(val system: ActorSystem,
                                   userService: UserService,
                                   authInfoRepository: AuthInfoRepository,
                                   articleService: ArticleService,
                                   avatarService: AvatarService,
                                   passwordHasher: PasswordHasher,
                                   @Named(BotArticleActor.name) val botArticleActor: ActorRef)(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  override def receive: Receive = {
    case "start_rss" =>
      log.error("start_rss")
      userService.getRssFeedUsers.map { uList =>
        uList map { u =>

          val rssItems = RssLinkCrawler.getRssItem(u.userProfile.getOrElse(UserProfile()).rssUrl.getOrElse(""))
          rssItems.zipWithIndex.map { case (rssItem, i) =>

            system.scheduler.scheduleOnce(
              FiniteDuration(i.toLong + 1, TimeUnit.MINUTES),
              botArticleActor, CreateArticleFromRssItem(rssItem, u))

          }
        }
      }

    case "start_link" =>
      log.error("start_link")
      userService.getRssFeedUsers.map { uList =>
        uList map { u =>

          val links = RssLinkCrawler.getLinks(u.userProfile.getOrElse(UserProfile()).rssUrl.getOrElse(""))
          links.zipWithIndex.map { case (link, i) =>

            system.scheduler.scheduleOnce(
              FiniteDuration(i.toLong + 1, TimeUnit.MINUTES),
              botArticleActor, CreateArticleFromLink(link, u))

          }
        }
      }

    case _ =>

  }

}

object BotRssCrawlerActor {

  final val name = "BotRssCrawlerActor"

}
