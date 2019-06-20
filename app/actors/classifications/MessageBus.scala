package actors.classifications

import akka.actor.ActorRef
import akka.event.{EventBus, SubchannelClassification}
import akka.util.Subclassification

/**
  * The base event.
  */

final case class MsgEnvelope(topic: String, payload: Any)


class StartsWithSubclassification extends Subclassification[String] {
  override def isEqual(x: String, y: String): Boolean =
    x == y

  override def isSubclass(x: String, y: String): Boolean =
    x.startsWith(y)
}


/**
  * An event bus implementation which uses a class based lookup classification.
  */
class MessageBus extends EventBus with SubchannelClassification {
  type Event = MsgEnvelope
  type Classifier = String
  type Subscriber = ActorRef

  /**
    * The logic to form sub-class hierarchy
    */

  override protected val subclassification: Subclassification[Classifier] =
    new StartsWithSubclassification


  // is used for extracting the classifier from the incoming events
  override protected def classify(event: Event): Classifier = event.topic

  // will be invoked for each event for all subscribers which registered
  // themselves for the event’s classifier
  override protected def publish(event: Event, subscriber: Subscriber): Unit = {
    subscriber ! event.payload
  }

}

/**
  * A global event bus instance.
  */
object MessageBus {

  /**
    * Holds the global event bus instance.
    */
  private lazy val instance = new MessageBus

  /**
    * Gets a global event bus instance.
    *
    * @return A global event bus instance.
    */
  def apply() = instance
}
