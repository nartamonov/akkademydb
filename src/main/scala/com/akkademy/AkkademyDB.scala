package com.akkademy

import akka.actor.Actor
import akka.event.Logging
import scala.collection.mutable
import com.akkademy.messages.SetRequest

class AkkademyDB extends Actor {
  private[akkademy] var storage = mutable.HashMap.empty[String,Object]
  private val log = Logging(this)

  override def receive: Receive = {
    case SetRequest(key, value) =>
      log.info("received SetRequest - key: {}, value: {}", key, value)
      storage.put(key, value)
    case m =>
      log.warning("received unexpected message - {}", m)
  }
}
