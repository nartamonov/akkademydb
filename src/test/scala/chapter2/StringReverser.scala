package chapter2

import akka.actor.{Status, Actor}

class StringReverser extends Actor {
  import StringReverser._

  override def receive: Receive = {
    case ReverseString(s) =>
      sender ! Status.Success(s.reverse)
    case x =>
      sender ! Status.Failure(new UnknownMessage(x))
  }
}

object StringReverser {
  case class ReverseString(s: String)
  case class UnknownMessage(message: Any) extends Exception
}