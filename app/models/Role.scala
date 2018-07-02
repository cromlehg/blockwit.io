package models

case class Role(
  val accountId: Long,
  val roleName: RoleName.RoleName) extends be.objectify.deadbolt.scala.models.Role {

  override val name = roleName.toString

}

object Role {

  val ADMIN = RoleName.ADMIN.toString

  val CLIENT = RoleName.CLIENT.toString

  def roleClient(accountId: Long) = new Role(accountId, RoleName.CLIENT)

  def roleAdmin(accountId: Long) = new Role(accountId, RoleName.ADMIN)

  def apply(accountId: Long, name: RoleName.RoleName): Role =
    new Role(accountId, name)

}

object RoleName extends Enumeration() {

  type RoleName = Value

  val CLIENT = Value("client")

  val EDITOR = Value("editor")

  val ADMIN = Value("admin")

}

