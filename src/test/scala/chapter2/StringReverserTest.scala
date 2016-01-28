package chapter2

import akka.actor.{ActorPath, Props, ActorSystem}
import akka.testkit.TestKit
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, FunSpecLike}

import scala.concurrent.Future

class StringReverserTest extends TestKit(ActorSystem("test-system")) with FunSpecLike with ScalaFutures with Matchers with BeforeAndAfterAll {
  val reverser = system.actorOf(Props[StringReverser], "reverser")
  val client = new Client(system.child("reverser"))

  implicit val executor = system.dispatcher

  it("should reverse string") {
    client.reverseString("abc").futureValue shouldBe "cba"
  }

  it("should reverse all strings") {
    val strings = Seq("abc", "bcd", "efg")
    Future.sequence(for (s <- strings) yield client.reverseString(s)).futureValue shouldBe Seq("cba", "dcb", "gfe")
  }

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)
}
