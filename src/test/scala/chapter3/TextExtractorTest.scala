package chapter3

import java.net.URI

import akka.actor.Status.Failure
import akka.actor.{ActorRef, Props, ActorSystem}
import akka.testkit.TestActor.AutoPilot
import akka.testkit.{ImplicitSender, TestActor, TestProbe, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, FunSpecLike}
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.duration._

class TextExtractorTest extends TestKit(ActorSystem("test-system")) with FunSpecLike with ScalaFutures with Matchers with BeforeAndAfterAll with ImplicitSender {
  import Cache._
  import HttpClient._
  import Extractor._
  import TextExtractor._

  val fakeCollaborators = Iterator.from(1).map(i => new {
    val httpClient = TestProbe("http-client")
    val cache = TestProbe("cache")
    val extractor = TestProbe("extractor")
    val textExtractor = system.actorOf(Props(classOf[TextExtractor], httpClient.testActor.path, extractor.testActor.path, cache.testActor.path), s"text-extractor-$i")
  })

  describe("Text extractor") {
    it("should fetch page if not in cache") {
      val fixture = fakeCollaborators.next()
      fixture.cache.setAutoPilot(alwaysNotFound())
      fixture.extractor.setAutoPilot(alwaysExtracting("extracted"))

      val uri = URI.create("http://google.com/")
      fixture.textExtractor ! ExtractTextFrom(uri)

      fixture.httpClient.expectMsg(FetchUri(uri))
      fixture.httpClient.reply(FetchingSuceeded(uri, "Some content"))

      expectMsg("extracted")
    }

    it("should get early extracted text from cache") {
      val fixture = fakeCollaborators.next()
      fixture.cache.setAutoPilot(alwaysFoundEntry(uri => Entry(uri, "extracted")))

      val uri = URI.create("http://google.com/")
      fixture.textExtractor ! ExtractTextFrom(uri)

      fixture.httpClient.expectNoMsg()
      fixture.extractor.expectNoMsg()

      expectMsg("extracted")
    }

    it("should fetch if cache timed out") {
      val fixture = fakeCollaborators.next()
      fixture.httpClient.setAutoPilot(alwaysFetching("fetched"))
      fixture.extractor.setAutoPilot(alwaysExtracting("extracted"))

      val uri = URI.create("http://google.com/")
      fixture.textExtractor ! ExtractTextFrom(uri)

      expectMsg("extracted")
    }

    it("should failed if fetching timed out") {
      val fixture = fakeCollaborators.next()
      fixture.cache.setAutoPilot(alwaysNotFound())

      val uri = URI.create("http://google.com/")
      fixture.textExtractor ! ExtractTextFrom(uri)

      expectMsgPF(Duration(8, SECONDS)) {
        case Failure(_) =>
      }
    }
  }

  def alwaysNotFound() = new AutoPilot {
    override def run(sender: ActorRef, msg: Any): AutoPilot = msg match {
      case GetEntryFor(uri) =>
        sender ! NotFound(uri)
        TestActor.NoAutoPilot
    }
  }

  def alwaysFoundEntry(f: URI => Entry) = new AutoPilot {
    override def run(sender: ActorRef, msg: Any): AutoPilot = msg match {
      case GetEntryFor(uri) =>
        sender ! f(uri)
        TestActor.NoAutoPilot
    }
  }

  def alwaysExtracting(text: String) = new AutoPilot {
    override def run(sender: ActorRef, msg: Any): AutoPilot = msg match {
      case Extract(_) =>
        sender ! text
        TestActor.NoAutoPilot
    }
  }

  def alwaysFetching(content: String) = new AutoPilot {
    override def run(sender: ActorRef, msg: Any): AutoPilot = msg match {
      case FetchUri(uri) =>
        sender ! FetchingSuceeded(uri, content)
        TestActor.NoAutoPilot
    }
  }

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)
}
