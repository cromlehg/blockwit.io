package models.daos

import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape.proveShapeOf

trait DBTableDefinitions {

  protected val driver: JdbcProfile
  import driver.api._

  class TelegramAccounts(tag: Tag) extends Table[models.TelegramAccount](tag, "telegram_accounts") {
    def accountId = column[Long]("account_id")
    def login     = column[String]("login")
    def * = (accountId, login) <> [models.TelegramAccount](t => 
         models.TelegramAccount(t._1, t._2), t => 
         Some((t.accountId, t.login)))
  }

  val telegramAccounts = TableQuery[TelegramAccounts]

  class Roles(tag: Tag) extends Table[models.Role](tag, "roles") {
    def userId = column[Long]("user_id")
    def role = column[Int]("role")
    def * = (userId, role) <> [models.Role](t => models.Role(t._1, t._2), models.Role.unapply)
  }

  val roles = TableQuery[Roles]
  
  class Sessions(tag: Tag) extends Table[models.Session](tag, "sessions") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("user_id")
    def ip = column[String]("ip")
    def sessionKey = column[String]("session_key")
    def created = column[Long]("created")
    def expire = column[Long]("expire")
    def * = (id, userId, ip, sessionKey, created, expire) <> (t =>
      models.Session(t._1, t._2, t._3, t._4, t._5, t._6), models.Session.unapply)
  }

  val sessions = TableQuery[Sessions]

  class Accounts(tag: Tag) extends Table[models.Account](tag, "accounts") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def login = column[String]("login")
    def email = column[String]("email")
    def hash = column[Option[String]]("hash")
    def confirmationStatus = column[Int]("confirmation_status")
    def accountStatus = column[Int]("account_status")
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

  val accounts = TableQuery[Accounts]
  
    class ShortOptions(tag: Tag) extends Table[models.ShortOption](tag, "short_options") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def value = column[String]("value")
    def ttype = column[String]("type")
    def descr = column[String]("descr")
    def * = (id, name, value, ttype, descr) <> [models.ShortOption](t =>
      models.ShortOption(t._1, t._2, t._3, t._4, t._5), models.ShortOption.unapply)
  }

  val shortOptions = TableQuery[ShortOptions]

  
}

