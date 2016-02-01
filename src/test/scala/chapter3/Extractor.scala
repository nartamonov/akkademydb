package chapter3

import akka.actor.Actor

class Extractor extends Actor {
  override def receive: Receive = ???
}

object Extractor {
  case class Extract(content: String)
}