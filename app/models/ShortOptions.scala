package models

case class ShortOption(
  val id: Long,
  val name: String,
  val value: String,
  val ttype: String,
  val descr: String) {

  def toBoolean = value.toBoolean

}

object ShortOption {

  def apply(
    id: Long,
    name: String,
    value: String,
    ttype: String,
    descr: String): ShortOption =
    new ShortOption(
      id,
      name,
      value,
      ttype,
      descr)

}

object ShortOptions {

  val TYPE_BOOLEAN = "Boolean"

  val REGISTER_ALLOWED = "REGISTER_ALLOWED"

}
