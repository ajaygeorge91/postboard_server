package actors.bots

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging}
import com.google.inject.Inject
import models.{AddSubComment, User, UserCommentActivity}
import models.services.CommentService

import scala.concurrent.ExecutionContext
import scala.util.Random

/**
  * Created by Android SD-1 on 23-03-2017.
  */

case class CreateSubComment(commentID: String, user: User)

case class ReadNewSubComment(commentID: String, user: User, page: Int)

case class ReadSubComment(commentID: String, user: User, page: Int)

class BotSubCommentActor @Inject()(commentService: CommentService)(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  override def receive: Receive = {

    case CreateSubComment(commentID: String, user: User) =>
      val content = if((Integer.parseInt(user.id) % 4) == 0){
        Contents.takeLoremRandomContentComment()
      }else{
        Contents.takeLoremRandomContentMalComment()
      }
      commentService.createSubComment(AddSubComment(content, commentID), user) map { sc =>
        log.error("SubComment created | " + sc.id + " | " + sc.content.getOrElse(""))
      }

    case ReadSubComment(commentID: String, user: User, page: Int) =>
      commentService.getHotSubCommentsForComment(commentID, Some(user), page, 10) map (l => l.result.map { sc =>
        if (Random.nextInt(10) > 8) {
          val len = Random.nextInt(sc.content.getOrElse("").split(" ").length)
          val rating = if (len >= 5 && len <= 25) 1 else -1
          commentService.addCommentActivityFromUser(user.id, sc.id, UserCommentActivity(rating = Some(rating))) map { a =>
            log.error("SubComment activity created | " + a.rating.getOrElse(0))
          }
        }
      })
      if (Random.nextInt(10) > 7) {
        self ! ReadSubComment(commentID, user, page + 1)
      }

    case ReadNewSubComment(commentID: String, user: User, page: Int) =>
      commentService.getNewSubCommentsForComment(commentID, Some(user), page, 10) map (l => l.result.map { sc =>
        if (Random.nextInt(10) > 5) {
          val len = Random.nextInt(sc.content.getOrElse("").split(" ").length)
          val rating = if (len >= 5 && len <= 15) 1 else -1
          commentService.addCommentActivityFromUser(user.id, sc.id, UserCommentActivity(rating = Some(rating))) map { a =>
            log.error("SubComment activity created | " + a.rating.getOrElse(0))
          }
        }
      })
      if (Random.nextInt(10) > 4) {
        self ! ReadNewSubComment(commentID, user, page + 1)
      }

  }
}

object BotSubCommentActor {
  final val name = "BotSubCommentActor"

}
