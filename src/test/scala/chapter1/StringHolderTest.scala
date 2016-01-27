package chapter1

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import chapter1.StringHolder.PutString
import org.scalatest.{BeforeAndAfterAll, Matchers, FunSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class StringHolderTest extends TestKit(ActorSystem("test-system")) with FunSpecLike with Matchers with BeforeAndAfterAll {
  describe("Given PutString") {
    it("should put string into internal storage") {
      val stringHolder = TestActorRef(new StringHolder)
      stringHolder ! PutString("abc")
      stringHolder.underlyingActor.string should equal(Some("abc"))
    }
  }

  describe("Given two subsequent PutString") {
    it("should contains second string inside internal storage") {
      val stringHolder = TestActorRef(new StringHolder)
      stringHolder ! PutString("abc")
      stringHolder ! PutString("bcd")
      stringHolder.underlyingActor.string should equal(Some("bcd"))
    }
  }

  override protected def afterAll(): Unit = Await.ready(system.terminate(), Duration.Inf)
}
