package com.akkademy

import akka.actor.{Props, ActorSystem}
import akka.event.Logging.Warning
import akka.testkit.{TestActorRef, TestKit}
import com.akkademy.messages.SetRequest
import org.scalatest.{Matchers, BeforeAndAfterAll, FunSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class AkkademyDBTest extends TestKit(ActorSystem("test-system")) with FunSpecLike with Matchers with BeforeAndAfterAll {
  describe("Given SetRequest") {
    it("should place key/value into internal storage") {
      val akkademyDB = TestActorRef(new AkkademyDB)
      akkademyDB ! SetRequest("k1", "v1")
      akkademyDB.underlyingActor.storage.get("k1") should equal(Some("v1"))
    }
  }

  describe("Given unexpected message") {
    it("should log warning") {
      val akkademyDB = system.actorOf(Props[AkkademyDB])
      system.eventStream.subscribe(testActor, classOf[Warning])

      akkademyDB ! "Hello!"

      expectMsgClass(classOf[Warning]).message.asInstanceOf[String] should include("received unexpected message")
    }
  }
  override protected def afterAll(): Unit = Await.ready(system.terminate(), Duration.Inf)
}
