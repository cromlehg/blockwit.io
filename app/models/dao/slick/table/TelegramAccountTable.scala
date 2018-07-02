package models.dao.slick.table

trait TelergamAccountTable extends CommonTable {

  import dbConfig.profile.api._
  
  class InnerCommonTable(tag: Tag) extends Table[models.TelegramAccount](tag, "telegram_accounts") {
    def accountId = column[Long]("account_id")
    def login     = column[String]("telegram_login")
    def * = (accountId, login) <> [models.TelegramAccount](t => 
         models.TelegramAccount(t._1, t._2), t => 
         Some((t.accountId, t.login)))
  }

  val table = TableQuery[InnerCommonTable]
  
}
