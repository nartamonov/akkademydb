package com.akkademy

package object messages {
  case class SetRequest(key: String, value: Object)
  case class SetIfNotExists(key: String, value: Object)
  case class Delete(key: String)
  case class GetRequest(key: String)
  case class KeyNotFoundException(key: String) extends Exception
}
