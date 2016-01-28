package com.akkademy

import akka.actor.{Props, ActorSystem}
import akka.testkit.TestKit
import com.akkademy.messages.KeyNotFoundException
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Span, Millis, Seconds}
import org.scalatest.{BeforeAndAfterAll, FunSpecLike, Matchers}

/**
  * Тестируем взаимодействие клиентского интерфейса с удаленной базой данных AkkademyDB.
  */
class IntegrationTest extends FunSpecLike with Matchers with ScalaFutures with BeforeAndAfterAll {
  val DBSystem = ActorSystem("akkademy", IntegrationTest.DBCfg)
  val ClientSystem = ActorSystem("client-system", IntegrationTest.ClientCfg)

  implicit val executor = ClientSystem.dispatcher
  implicit override val patienceConfig =
    PatienceConfig(timeout = Span(2, Seconds), interval = Span(100, Millis))

  val akkademyDB = DBSystem.actorOf(Props[AkkademyDB], "akkademy-db")
  val client = new Client("127.0.0.1:2552")(ClientSystem)

  it("should raise exception if requested non-existent key") {
    client.get("k").failed.futureValue shouldBe a[KeyNotFoundException]
  }

  it("should returns last set value") {
    val fut = for (
      _ <- client.set("k", "v");
      v <- client.get("k")
    ) yield v

    fut.futureValue should equal("v")
  }

  it("should set value if not exists before") {
    val fut = for (
      _ <- client.setIfNotExists("k1", "v1");
      v <- client.get("k1")
    ) yield v

    fut.futureValue shouldBe "v1"
  }

  it("should not set value if it exists already") {
    val fut = for (
      _ <- client.setIfNotExists("k", "v1");
      v <- client.get("k")
    ) yield v

    fut.futureValue shouldBe "v"
  }

  it("should delete key") {
    val fut = for (
      _ <- client.delete("k");
      v <- client.get("k")) yield v

    fut.failed.futureValue shouldBe a[KeyNotFoundException]
  }

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(ClientSystem)
    TestKit.shutdownActorSystem(DBSystem)
  }
}

object IntegrationTest {
  val DBCfg = ConfigFactory.parseString(
    """
      |akka {
      |  actor {
      |    provider = "akka.remote.RemoteActorRefProvider"
      |  }
      |
      |  remote {
      |    enabled-transports = ["akka.remote.netty.tcp"]
      |    netty.tcp {
      |      hostname = "127.0.0.1"
      |      port = 2552
      |    }
      |  }
      |}
    """.stripMargin
  )

  val ClientCfg = ConfigFactory.parseString(
    """
      |akka {
      |  actor {
      |    provider = "akka.remote.RemoteActorRefProvider"
      |  }
      |
      |  remote {
      |    enabled-transports = ["akka.remote.netty.tcp"]
      |    netty.tcp {
      |      hostname = "127.0.0.1"
      |      port = 2553
      |    }
      |  }
      |}
    """.stripMargin
  )
}