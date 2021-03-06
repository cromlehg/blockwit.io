package security

import be.objectify.deadbolt.scala.HandlerKey

object HandlerKeys {

  val defaultHandler = Key("defaultHandler")
  val altHandler = Key("altHandler")
  val userlessHandler = Key("userlessHandler")

  case class Key(name: String) extends HandlerKey

}