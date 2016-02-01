package chapter3

import java.net.URI

import akka.actor.Status.Failure
import akka.actor.{ActorRef, Actor, Props, ActorPath}

import scala.concurrent.duration._

/**
  * Извлекает читабельный текст из веб-страниц.
  */
class TextExtractor(httpClientPath: ActorPath, extractorPath: ActorPath, cachePath: ActorPath) extends Actor {
  import TextExtractor._
  import Cache._
  import HttpClient._
  import Extractor._

  val httpClient = context.actorSelection(httpClientPath)
  val extractor = context.actorSelection(extractorPath)
  val cache = context.actorSelection(cachePath)

  override def receive: Receive = {
    case ExtractTextFrom(uri) =>
      val replyTo = sender()
      context.actorOf(Props(new RequestProcessor(uri, replyTo)), "request-processor")
  }

  private class RequestProcessor(uri: URI, replyTo: ActorRef) extends Actor {
    private case object TimeoutExpired

    import context.dispatcher

    @throws[Exception](classOf[Exception])
    override def preStart(): Unit = {
      cache ! GetEntryFor(uri)
      context.system.scheduler.scheduleOnce(Duration(2, SECONDS), self, TimeoutExpired)
    }

    override def receive: Receive = waitEntryFromCache

    def waitEntryFromCache: Receive = {
      case Entry(_, text) =>
        replyTo ! text
        context.stop(self)
      case NotFound(_) | TimeoutExpired =>
        httpClient ! FetchUri(uri)
        context.become(waitWhileFetching)
        context.system.scheduler.scheduleOnce(Duration(5, SECONDS), self, TimeoutExpired)
    }

    def waitWhileFetching: Receive = {
      case FetchingSuceeded(_, content) =>
        extractor ! Extract(content)
        context.become(waitWhileExtracting)
      case FetchingFailed(reason) =>
        replyTo ! Failure(new Exception("Failed to fetch page", reason))
        context.stop(self)
      case TimeoutExpired =>
        replyTo ! Failure(new Exception("Fetching was timed out"))
        context.stop(self)
    }

    def waitWhileExtracting: Receive = {
      case m: String =>
        cache ! PutEntry(uri, m)
        replyTo ! m
        context.stop(self)
    }
  }
}

object TextExtractor {
  case class ExtractTextFrom(uri: URI)
}
