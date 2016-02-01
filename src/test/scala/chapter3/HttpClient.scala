package chapter3

import java.net.URI

import akka.actor.Actor

private class HttpClient extends Actor {
  import HttpClient._

  override def receive: Receive = {
    case FetchUri(uri) =>
      sender() ! FetchingSuceeded(uri, "<html><body>Hello, world!</body></html>")
  }
}

private object HttpClient {
  case class FetchUri(uri: URI)
  case class FetchingFailed(reason: Throwable)
  case class FetchingSuceeded(uri: URI, content: String)
}