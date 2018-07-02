package models.dao.slick.table

import play.api.db.slick.HasDatabaseConfigProvider

trait SessionTable extends CommonTable {

  import dbConfig.profile.api._

  class InnerCommonTable(tag: Tag) extends Table[models.Session](tag, "sessions") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("user_id")
    def ip = column[String]("ip")
    def sessionKey = column[String]("session_key")
    def created = column[Long]("created")
    def expire = column[Long]("expire")
    def * = (id, userId, ip, sessionKey, created, expire) <> (t =>
      models.Session(t._1, t._2, t._3, t._4, t._5, t._6), models.Session.unapply)
  }

  val table = TableQuery[InnerCommonTable]
  
}
