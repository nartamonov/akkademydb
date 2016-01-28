package com.akkademy

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future

class Client(remoteAddress: String)(implicit system: ActorSystem) {
  import messages._

  private implicit val timeout = Timeout(2, TimeUnit.SECONDS)

  private val akkademyDB = system.actorSelection(s"akka.tcp://akkademy@$remoteAddress/user/akkademy-db")

  def set(key: String, value: Object): Future[_] = {
    akkademyDB ? SetRequest(key, value)
  }

  def setIfNotExists(key: String, value: Object): Future[_] = {
    akkademyDB ? SetIfNotExists(key, value)
  }

  def get(key: String): Future[Any] = {
    akkademyDB ? GetRequest(key)
  }

  def delete(key: String): Future[_] = {
    akkademyDB ? Delete(key)
  }
}
