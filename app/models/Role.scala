package models

case class Role(
  val userId: Long,
  val role: Int)

object Role {

  def apply(userId: Long, role: Int): Role = new Role(userId, role)

}