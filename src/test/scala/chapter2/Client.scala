package chapter2

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, ActorPath}
import akka.pattern.ask
import akka.util.Timeout
import chapter2.StringReverser.ReverseString

import scala.concurrent.Future

class Client(reverserPath: ActorPath)(implicit system: ActorSystem) {
  private val reverser = system.actorSelection(reverserPath)
  private implicit val timeout = Timeout(2, TimeUnit.SECONDS)

  def reverseString(s: String): Future[String] = {
    (reverser ? ReverseString(s)).mapTo[String]
  }
}
