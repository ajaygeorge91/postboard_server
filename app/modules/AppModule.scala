package modules

import actors.{S3SenderActor, UserNotificationActor}
import actors.bots._
import com.google.inject.AbstractModule
import models.daos._
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport

/**
  * The Guice module which wires all Silhouette dependencies.
  */
class AppModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  /**
    * Configures the module.
    */
  def configure() {

    bind[ArticleDAO].to[ArticleDAOImpl]
    bind[CommentDAO].to[CommentDAOImpl]
    bind[UserArticleActivityDAO].to[UserArticleActivityDAOImpl]
    bind[UserCommentActivityDAO].to[UserCommentActivityDAOImpl]
    bind[UserActivityDAO].to[UserActivityDAOImpl]
    bind[UserNotificationDAO].to[UserNotificationDAOImpl]
    bind[SearchDAO].to[SearchDAOImpl]

    //    bindActor[SubscribeActor]("SubscribeActor")

    bindActor[UserNotificationActor](UserNotificationActor.name)
    bindActor[S3SenderActor](S3SenderActor.name)

    bindActor[BotSubCommentActor](BotSubCommentActor.name)
    bindActor[BotCommentActor](BotCommentActor.name)
    bindActor[BotArticleActor](BotArticleActor.name)
    bindActor[BotRssCrawlerActor](BotRssCrawlerActor.name)
    bindActor[BotUserActor](BotUserActor.name)
    bind(classOf[BotScheduler]).asEagerSingleton()
  }

}
