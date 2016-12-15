package com.yicheng.statistics.design.pattern

import akka.actor._
import reflect.runtime.currentMirror

/**
  * Created by yuer on 2016/12/5.
  */
case class InterestedIn(messageType: String)
case class NoLongerInterestedIn(messageType: String)

case class TypeAMessage(description: String)
case class TypeBMessage(description: String)
case class TypeCMessage(description: String)
case class TypeDMessage(description: String)

object DynamicRouterDriver extends CompletableApp(5) {
  val dunnoInterested = system.actorOf(Props[DunnoInterested], "dunnoInterested")

  val typedMessageInterestRouter =
    system.actorOf(Props(
      new TypedMessageInterestRouter(dunnoInterested, 4, 1)),
      "typedMessageInterestRouter")

  val typeAInterest = system.actorOf(Props(classOf[TypeAInterested], typedMessageInterestRouter), "typeAInterest")
  val typeBInterest = system.actorOf(Props(classOf[TypeBInterested], typedMessageInterestRouter), "typeBInterest")
  val typeCInterest = system.actorOf(Props(classOf[TypeCInterested], typedMessageInterestRouter), "typeCInterest")
  val typeCAlsoInterested = system.actorOf(Props(classOf[TypeCAlsoInterested], typedMessageInterestRouter), "typeCAlsoInterested")

  awaitCanStartNow

  typedMessageInterestRouter ! TypeAMessage("Message of TypeA.")
  typedMessageInterestRouter ! TypeBMessage("Message of TypeB.")
  typedMessageInterestRouter ! TypeCMessage("Message of TypeC.")

  awaitCanCompleteNow

  typedMessageInterestRouter ! TypeCMessage("Another message of TypeC.")
  typedMessageInterestRouter ! TypeDMessage("Message of TypeD.")

  awaitCompletion
  println("DynamicRouter: is completed.")
}

class TypedMessageInterestRouter(
                                  dunnoInterested: ActorRef,
                                  canStartAfterRegistered: Int,
                                  canCompleteAfterUnregistered: Int) extends Actor {

  val interestRegistry = scala.collection.mutable.Map[String, ActorRef]()
  val secondaryInterestRegistry = scala.collection.mutable.Map[String, ActorRef]()

  def receive = {
    case interestedIn: InterestedIn =>
      registerInterest(interestedIn)
    case noLongerInterestedIn: NoLongerInterestedIn =>
      unregisterInterest(noLongerInterestedIn)
    case message: Any =>
      sendFor(message)
  }

  def registerInterest(interestedIn: InterestedIn) = {
    val messageType = typeOfMessage(interestedIn.messageType)
    if (!interestRegistry.contains(messageType)) {
      interestRegistry(messageType) = sender
    } else {
      secondaryInterestRegistry(messageType) = sender
    }

    if (interestRegistry.size + secondaryInterestRegistry.size >= canStartAfterRegistered) {
      DynamicRouterDriver.canStartNow()
    }
  }

  def sendFor(message: Any) = {
    val messageType = typeOfMessage(currentMirror.reflect(message).symbol.toString)

    if (interestRegistry.contains(messageType)) {
      interestRegistry(messageType) forward message
    } else {
      dunnoInterested ! message
    }
  }

  def typeOfMessage(rawMessageType: String): String = {
    rawMessageType.replace('$', ' ').replace('.', ' ').split(' ').last.trim
  }

  var unregisterCount: Int = 0

  def unregisterInterest(noLongerInterestedIn: NoLongerInterestedIn) = {
    val messageType = typeOfMessage(noLongerInterestedIn.messageType)

    if (interestRegistry.contains(messageType)) {
      val wasInterested = interestRegistry(messageType)

      if (wasInterested.compareTo(sender) == 0) {
        if (secondaryInterestRegistry.contains(messageType)) {
          val nowInterested = secondaryInterestRegistry.remove(messageType)

          interestRegistry(messageType) = nowInterested.get
        } else {
          interestRegistry.remove(messageType)
        }

        unregisterCount = unregisterCount + 1;
        if (unregisterCount >= this.canCompleteAfterUnregistered) {
          DynamicRouterDriver.canCompleteNow()
        }
      }
    }
  }
}

class DunnoInterested extends Actor {
  def receive = {
    case message: Any =>
      println(s"DunnoInterest: received undeliverable message: $message")
      DynamicRouterDriver.completedStep()
  }
}

class TypeAInterested(interestRouter: ActorRef) extends Actor {
  interestRouter ! InterestedIn(TypeAMessage.getClass.getName)

  def receive = {
    case message: TypeAMessage =>
      println(s"TypeAInterested: received: $message")
      DynamicRouterDriver.completedStep()
    case message: Any =>
      println(s"TypeAInterested: received unexpected message: $message")
  }
}

class TypeBInterested(interestRouter: ActorRef) extends Actor {
  interestRouter ! InterestedIn(TypeBMessage.getClass.getName)

  def receive = {
    case message: TypeBMessage =>
      println(s"TypeBInterested: received: $message")
      DynamicRouterDriver.completedStep()
    case message: Any =>
      println(s"TypeBInterested: received unexpected message: $message")
  }
}

class TypeCInterested(interestRouter: ActorRef) extends Actor {
  interestRouter ! InterestedIn(TypeCMessage.getClass.getName)

  def receive = {
    case message: TypeCMessage =>
      println(s"TypeCInterested: received: $message")

      interestRouter ! NoLongerInterestedIn(TypeCMessage.getClass.getName)

      DynamicRouterDriver.completedStep()

    case message: Any =>
      println(s"TypeCInterested: received unexpected message: $message")
  }
}

class TypeCAlsoInterested(interestRouter: ActorRef) extends Actor {
  interestRouter ! InterestedIn(TypeCMessage.getClass.getName)

  def receive = {
    case message: TypeCMessage =>
      println(s"TypeCAlsoInterested: received: $message")

      interestRouter ! NoLongerInterestedIn(TypeCMessage.getClass.getName)

      DynamicRouterDriver.completedStep()
    case message: Any =>
      println(s"TypeCAlsoInterested: received unexpected message: $message")
  }
}
