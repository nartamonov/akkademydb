package com.akkademy

import akka.actor.{Status, Actor}
import akka.event.Logging
import scala.collection.mutable
import com.akkademy.messages._

class AkkademyDB extends Actor {
  private[akkademy] var storage = mutable.HashMap.empty[String,Object]
  private val log = Logging(this)

  override def receive: Receive = {
    case SetRequest(key, value) =>
      log.info("received SetRequest - key: {}, value: {}", key, value)
      storage.put(key, value)
      sender ! Status.Success
    case SetIfNotExists(key, value) =>
      log.info("received SetIfNotExists - key: {}, value: {}", key, value)
      storage.getOrElseUpdate(key, value)
      sender ! Status.Success
    case GetRequest(key) =>
      log.info("received GetRequest - key: {}", key)
      storage.get(key) match {
        case Some(v) => sender ! v
        case None => sender ! Status.Failure(new KeyNotFoundException(key))
      }
    case Delete(key) =>
      log.info("received Delete - key: {}", key)
      storage.remove(key)
      sender ! Status.Success
    case m =>
      log.warning("received unexpected message - {}", m)
  }
}
