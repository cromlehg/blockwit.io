package models.dao.slick.table

import play.api.db.slick.DatabaseConfigProvider
import models.AccountStatus
import models.ConfirmationStatus

trait AccountTable extends CommonTable {

  import dbConfig.profile.api._
  
  implicit val AccountStatusMapper = enum2String(AccountStatus)
  
  implicit val ConfirmationStatusMapper = enum2String(ConfirmationStatus)

  class InnerCommonTable(tag: Tag) extends Table[models.Account](tag, "accounts") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def login = column[String]("login")
    def email = column[String]("email")
    def hash = column[Option[String]]("hash")
    def confirmationStatus = column[ConfirmationStatus.ConfirmationStatus]("confirmation_status")
    def accountStatus = column[AccountStatus.AccountStatus]("account_status")
    def registered = column[Long]("registered")
    def confirmCode = column[Option[String]]("confirm_code")
    def * = (
      id,
      login,
      email,
      hash,
      confirmationStatus,
      accountStatus,
      registered,
      confirmCode) <> [models.Account](t => models.Account(
            t._1,
            t._2,
            t._3,
            t._4,
            t._5,
            t._6,
            t._7,
            t._8), t => Some((
      t.id,
      t.login,
      t.email,
      t.hash,
      t.confirmationStatus,
      t.accountStatus,
      t.registered,
      t.confirmCode)))

  }

  val table = TableQuery[InnerCommonTable]
  
}
