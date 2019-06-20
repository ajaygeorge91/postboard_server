package actors.bots

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

/**
  * Created by Android SD-1 on 22-03-2017.
  */
class BotScheduler @Inject()(val system: ActorSystem,
                             @Named(BotUserActor.name) val botUserActor: ActorRef,
                             @Named(BotRssCrawlerActor.name) val botRssCrawlerActor: ActorRef
                            )(implicit ec: ExecutionContext) {

  system.scheduler.schedule(
    FiniteDuration(5, TimeUnit.MINUTES),
    FiniteDuration(5, TimeUnit.MINUTES),
    botUserActor, "start")

  system.scheduler.schedule(
    FiniteDuration(1, TimeUnit.MINUTES),
    FiniteDuration(10, TimeUnit.MINUTES),
    botRssCrawlerActor, "start_rss")

}
