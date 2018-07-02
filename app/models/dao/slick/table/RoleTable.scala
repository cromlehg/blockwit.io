package models.dao.slick.table

import play.api.db.slick.HasDatabaseConfigProvider
import models.RoleName

trait RoleTable extends CommonTable {

  import dbConfig.profile.api._

  implicit val RoleNameMapper = enum2String(RoleName)
  
  class InnerCommonTable(tag: Tag) extends Table[models.Role](tag, "roles") {
    def accountId = column[Long]("account_id")
    def roleName = column[RoleName.RoleName]("role_name")
    def * = (accountId, roleName) <> [models.Role](t => models.Role(t._1, t._2), models.Role.unapply)
  }

  val table = TableQuery[InnerCommonTable]
  
}
