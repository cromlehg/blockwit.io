package models.dao.slick.table

import play.api.db.slick.HasDatabaseConfigProvider

trait ShortOptionTable extends CommonTable {

  import dbConfig.profile.api._

  class InnerCommonTable(tag: Tag) extends Table[models.ShortOption](tag, "short_options") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def value = column[String]("value")
    def ttype = column[String]("type")
    def descr = column[String]("descr")
    def * = (id, name, value, ttype, descr) <> [models.ShortOption](t =>
      models.ShortOption(t._1, t._2, t._3, t._4, t._5), models.ShortOption.unapply)
  }

  val table = TableQuery[InnerCommonTable]
  
}
