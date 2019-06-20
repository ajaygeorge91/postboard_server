package actors.bots

import javax.inject.Named

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.google.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.services.UserService
import models.{ProfileAvatar, User, UserProfile}
import utils.ProfileUtils

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

/**
  * Created by Android SD-1 on 22-03-2017.
  */


class BotUserActor @Inject()(userService: UserService,
                             authInfoRepository: AuthInfoRepository,
                             avatarService: AvatarService,
                             passwordHasher: PasswordHasher,
                             @Named(BotArticleActor.name) val botArticleActor: ActorRef)(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  def createArticle(u: User): Unit = {
    botArticleActor ! Create(u)
  }

  def readArticles(u: User): Unit = {
    botArticleActor ! Read(u, 0)
  }

  def readArticlesNew(u: User): Unit = {
    botArticleActor ! ReadNew(u, 0)
  }

  override def receive: Receive = {
    case "start" =>
      log.error("start")
      val userId = Random.nextInt(100).toString
      val userResult = userService.getFromID(userId) flatMap {
        case Some(u) =>
          Future.successful(u)
        case None =>
          val email = userId.concat("@example.com")
          val loginInfo = LoginInfo(CredentialsProvider.ID, email)
          val authInfo = passwordHasher.hash("password")
          val userProfile = UserProfile(
            email = Some(email)
          )
          val user = User(
            id = "",
            loginInfo = Some(loginInfo),
            fullName = Some(Random.shuffle(BotUserActor.nameArray.toList).head),
            avatarURL = None
          )
          for {
            avatar <- avatarService.retrieveURL(email)
            user <- userService.save(user.copy(avatarURL = avatar, userProfile = Some(ProfileUtils.addAvatar(ProfileAvatar("gravatar", avatar), Some(userProfile)))))
            authInfo <- authInfoRepository.add(loginInfo, authInfo)
          } yield {
            user
          }
      }
      userResult map { u =>
        log.error("User | " + u.id.toString)
        Random.nextInt(10) match {
          case 0 =>
            createArticle(u)
          //            readArticles(u)
          case 1 =>
            readArticlesNew(u)
          case 2 =>
            readArticlesNew(u)
          case _ =>
            readArticles(u)
        }
      }
  }

}

object BotUserActor {

  final val name = "BotUserActor"

  final val nameArray = Array("Phil Peake", "Stephanie Johnston", "Sean Lee", "Cameron Roberts", "Elizabeth Hudson", "Nicholas Langdon", "Diane Cornish", "Trevor Turner", "Penelope Lewis", "Jacob Payne", "Benjamin Baker", "Donna Sutherland", "Ella Davidson", "Robert Wilson", "Diana Davidson", "Ruth James", "Dominic Walsh", "Sam Lee", "Molly Hardacre", "Cameron Gray", "Sean Vaughan", "Sophie Edmunds", "Audrey Grant", "Angela Morgan", "Anthony Parsons", "Wanda Abraham", "Evan Tucker", "Alexandra Clark", "Amy Ball", "Michelle Kelly", "Chloe Turner", "Stephen Gray", "Leah Howard", "Victoria Hodges", "Sally Henderson", "Charles Skinner", "Liam Slater", "Carl Lambert", "Natalie Burgess", "Diana Hamilton", "Joseph Gibson", "Rose Bell", "Lily Parsons", "Paul Lee", "Brian Underwood", "Samantha Greene", "Carolyn Dyer", "Madeleine Young", "Ian Powell", "Sonia Lyman", "Ella Springer", "Yvonne Lewis", "Stephanie Peters", "Carol Nolan", "Carl Ellison", "Jason Payne", "Tim North", "Donna Kelly", "Alan Gill", "Yvonne Walsh", "Diana Graham", "Gordon Dyer", "Dorothy Harris", "Alison Lee", "Kylie Fraser", "Hannah Graham", "Lauren Paterson", "Felicity Newman", "Joshua McLean", "Karen McGrath", "Maria Oliver", "Jake Ince", "Stewart Payne", "Vanessa Wright", "Ella May", "Molly Knox", "Benjamin Turner", "Samantha Lambert", "Carol Bailey", "Katherine Sanderson", "Joan Russell", "Oliver Buckland", "Nathan Terry", "Sally Ogden", "Boris Berry", "Andrea Robertson", "Ella Simpson", "Katherine Lambert", "Kevin Morrison", "Yvonne Rampling", "Victor Robertson", "Rachel Hodges", "Yvonne Robertson", "Emily Peake", "Virginia Dyer", "Pippa Randall", "Jennifer Underwood", "Steven Young", "Max Tucker", "Jasmine Cameron", "Max Davies", "Dan Campbell", "Amy Pullman", "Andrea Edmunds", "Oliver North", "Brandon Lyman", "Alison Wright", "Blake Hodges", "Edward Knox", "Samantha Glover", "Brandon Peters", "James Bell", "Una Lee", "Dan Smith", "Liam Howard", "Ryan Taylor", "Mary Buckland", "Matt Brown", "Lucas Sharp", "Jasmine McGrath", "Vanessa Gibson", "Ella Fisher", "Molly Kerr", "Victor Hunter", "Luke Piper", "Keith Scott", "Karen McLean", "Matt Powell", "Kimberly Mills", "Dorothy Skinner", "Sean Coleman", "Peter White", "Samantha Ellison", "Ava Ince", "Sonia Robertson", "Elizabeth Murray", "Victor Dowd", "Maria Gray", "Jacob Newman", "Peter Morrison", "Stephen Morrison", "Amy Burgess", "Boris Butler", "Leonard Wright", "Dorothy Morgan", "Piers Henderson", "Zoe Knox", "Gavin Peake", "Warren Rees", "Leah McDonald", "Olivia Glover", "James Edmunds", "Caroline Berry", "Felicity Tucker", "Joan Thomson", "Jake Quinn", "Dylan Lee", "Trevor Brown", "Gavin Kerr", "Ava Alsop", "Alan Gray", "Lillian Ferguson", "Madeleine McDonald", "Alison Sutherland", "Andrea Thomson", "Ian Jones", "Joshua Edmunds", "Colin Vance", "Max Bell", "Leah Hunter", "Michael Hamilton", "Paul Rutherford", "Melanie Quinn", "Simon Hodges", "Keith Hughes", "Alan Parr", "Richard Vance", "Deirdre Davidson", "Lauren Peake", "Rose Watson", "Ava Scott", "Nathan Ogden", "Steven Mills", "Alexandra Graham", "Peter Glover", "Kevin Campbell", "Boris Vaughan", "Eric Russell", "Sonia Mackay", "Victor Jones", "Lucas Wilson", "Ian Marshall", "Robert Thomson", "Diane Springer", "Keith Kelly", "Stewart Gibson", "Simon Gill", "Bernadette Parsons", "Trevor Oliver", "Kimberly Kelly", "Rachel Greene", "Tracey Newman", "Adam Harris", "Charles Hodges", "Matt Clark", "Kylie Simpson", "Peter Clark", "Leah Rutherford", "Jasmine Jones", "Piers Sutherland", "Virginia White", "Benjamin Miller", "Dominic Graham", "Paul Cornish", "Andrew Wilson", "Piers Payne", "Chloe Welch", "David Nolan", "Jonathan Ross", "Liam Lawrence", "Frank Skinner", "Olivia Sanderson", "Yvonne Simpson", "Heather Blake", "Heather Harris", "Wendy Graham", "Audrey Lambert", "Bernadette Sharp", "Cameron Rutherford", "Megan Bower", "Owen Watson", "Leah Fraser", "Jennifer Hemmings", "Maria Gibson", "Madeleine Wallace", "Evan Allan", "Simon Smith", "Julian Ellison", "Keith Berry", "James Mackay", "Zoe Hodges", "Emily Graham", "Victor Ogden", "Joan Pullman", "Emily Quinn", "Joshua Robertson", "Charles Nolan", "Charles Mackenzie", "Steven Wilkins", "Dylan Johnston", "Mary Morgan", "Wanda Churchill", "Sue Wright", "Justin Bower", "Mary Vance", "Carl Morrison", "Blake Lyman", "Irene Mackay", "Frank North", "Tracey Piper", "Charles Ellison", "Wendy Cameron", "Theresa Parr", "Madeleine Edmunds", "Virginia Underwood", "Jan McGrath", "Michelle Henderson", "Joanne Greene", "Amelia Young", "Max Morrison", "Alison Taylor", "Leah Short", "Neil Dowd", "Thomas Duncan", "Julia Bell", "Amelia Morgan", "John Smith", "Joseph Hill", "Irene Reid", "Leah Hemmings", "Victor Taylor")

}