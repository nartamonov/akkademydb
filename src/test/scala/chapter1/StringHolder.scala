package chapter1

import akka.actor.Actor

class StringHolder extends Actor {
  import StringHolder._

  private[chapter1] var string = Option.empty[String]

  override def receive: Receive = {
    case PutString(s) =>
      string = Some(s)
  }
}

object StringHolder {
  case class PutString(s: String)
}