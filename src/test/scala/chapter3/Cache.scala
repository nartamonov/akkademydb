package chapter3

import java.net.URI

import akka.actor.Actor

private class Cache extends Actor {
  import Cache._

  override def receive: Receive = withState(Map.empty)

  def withState(cache: Map[URI, String]): Receive = {
    case GetEntryFor(uri) =>
      cache.get(uri) match {
        case Some(text) => sender() ! Entry(uri, text)
        case None => sender() ! NotFound(uri)
      }
    case PutEntry(uri, text) =>
      context.become(withState(cache.updated(uri, text)))
  }
}

private object Cache {
  case class GetEntryFor(uri: URI)
  case class PutEntry(uri: URI, text: String)
  case class NotFound(uri: URI)
  case class Entry(uri: URI, text: String)
}