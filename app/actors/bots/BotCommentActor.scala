package actors.bots

import javax.inject.Named

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, ActorRef}
import com.google.inject.Inject
import models.{AddComment, User, UserCommentActivity}
import models.services.{ArticleService, CommentService}

import scala.concurrent.ExecutionContext
import scala.util.Random

/**
  * Created by Android SD-1 on 22-03-2017.
  */


case class CreateComment(articleID: String, user: User)

case class ReadComment(articleID: String, user: User, page: Int)

case class ReadNewComment(articleID: String, user: User, page: Int)


class BotCommentActor @Inject()(commentService: CommentService,
                                @Named(BotSubCommentActor.name) val botSubCommentActor: ActorRef)(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  def subCommentAction(user: User, commentID: String): Unit = {
    Random.nextInt(3) match {
      case 0 =>
        botSubCommentActor ! CreateSubComment(commentID, user)
      case 1 =>
        botSubCommentActor ! ReadNewSubComment(commentID, user, 0)
      case 2 =>
        botSubCommentActor ! ReadSubComment(commentID, user, 0)
    }
  }

  override def receive: Receive = {
    case CreateComment(articleID: String, user: User) =>
      val content = if((Integer.parseInt(user.id) % 4) == 0){
        Contents.takeLoremRandomContentComment()
      }else{
        Contents.takeLoremRandomContentMalComment()
      }
      commentService.createComment(AddComment(content = content, articleID = articleID), user) map { c =>
        log.error("Comment created | " + c.id + " | " + c.content.getOrElse(""))
      }
    case ReadComment(articleID: String, user: User, page: Int) =>
      commentService.getHotCommentsForArticle(articleID, Some(user), page, 10) map (l => l.result.foreach { c =>
        if (Random.nextInt(100) > 80) {
          val len = Random.nextInt(c.content.getOrElse("").split(" ").length)
          val rating = if (len >= 5 && len <= 25) 1 else -1
          commentService.addCommentActivityFromUser(user.id, c.id, UserCommentActivity(rating = Some(rating))) map { a =>
            log.error("Comment activity created | " + a.rating.getOrElse(0))
          }
        }
        if (Random.nextInt(100) > 70) {
          subCommentAction(user, c.id)
        }
      })
      if (Random.nextInt(100) > 60) {
        self ! ReadComment(articleID, user, page + 1)
      }
    case ReadNewComment(articleID: String, user: User, page: Int) =>
      commentService.getNewCommentsForArticle(articleID, Some(user), page, 10) map (l => l.result.foreach { c =>
        if (Random.nextInt(100) > 50) {
          val len = Random.nextInt(c.content.getOrElse("").split(" ").length)
          val rating = if (len >= 5 && len <= 15) 1 else -1
          commentService.addCommentActivityFromUser(user.id, c.id, UserCommentActivity(rating = Some(rating))) map { a =>
            log.error("Comment activity created | " + a.rating.getOrElse(0))
          }
        }
        if (Random.nextInt(100) > 50) {
          subCommentAction(user, c.id)
        }
      })
      if (Random.nextInt(100) > 40) {
        self ! ReadNewComment(articleID, user, page + 1)
      }
  }

}

object BotCommentActor {
  final val name = "BotCommentActor"

}