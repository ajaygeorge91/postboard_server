package actors

import akka.actor._

/**
  * Created by ajay on 07-Apr-16.
  */


class WebSocketDashboardActor(out: ActorRef) extends Actor with ActorLogging {

  def receive = {
    case msg: String =>
      out ! ("I received your message: " +
        msg+"\n"+
        "out.path.toSerializationFormat : "+out.path.toSerializationFormat+"\n"+
        "out.path.name : "+out.path.name+"\n"+
        "out.path.address.system : "+out.path.address.system+"\n"+
        "out.path.toStringWithoutAddress : "+out.path.toStringWithoutAddress+"\n"+
        "\n"+
        "\n"+
        out.path.toSerializationFormat)
  }

}


object WebSocketDashboardActor {

  final val NAME = "WebSocketDashboardActor"

  def props(out: ActorRef) = Props(new WebSocketDashboardActor(out))

}
