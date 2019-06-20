package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.sandinh.paho.akka._
import models.{User, WebsocketMessage}
import models.services.{UserNotificationService, UserService}
import org.apache.commons.lang3.ArrayUtils
import utils.{Constants, MqttUtils}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by ajayg on 10/16/2016.
  */


class SubscribeActor(user: User, userNotificationService: UserNotificationService, out: ActorRef) extends Actor with ActorLogging {

  val pubsub: ActorRef = context.actorOf(Props(classOf[MqttPubSub], PSConfig(
    brokerUrl = "tcp://localhost:1883", //all params is optional except brokerUrl
    userName = null,
    password = null
  )))

  pubsub ! Subscribe("test", self)
  pubsub ! Subscribe(MqttUtils.getNotificationChannelForUser(user.id), self)

  def receive: PartialFunction[Any, Unit] = {
    case SubscribeAck(Subscribe(topic, ref_self, _), fail) =>
      if (fail.isEmpty) {
        log.error(s"subscribed to $topic")
        if(topic.equalsIgnoreCase(MqttUtils.getNotificationChannelForUser(user.id))){
          userNotificationService.getUserNotificationsConnectedToUser(user.id,0,Constants.DEFAULT_NO_OF_RECORDS)map{nL =>
            pubsub ! Publish(topic, MqttUtils.getNotificationListAsByteArray(nL))
          }
        }
      }
      else log.error(fail.get, s"Can't subscribe to $topic")

    case msg: Message =>

      log.error(s"Hi ${user.fullName}, I received your message: " + msg.topic + " " + MqttUtils.convertMessageToString(msg))
      val js = MqttUtils.convertMessageToString(msg)
      out ! js

    case msg: String =>
      log.error(s"Hi ${user.fullName}, I received your message: " + msg)
      out ! (s"Hi ${user.fullName}, I received your message: " + msg)
  }

}

object SubscribeActor {
  def props(user: User, userNotificationService: UserNotificationService)(out: ActorRef) = Props(new SubscribeActor(user,userNotificationService, out))
}

