package models.dao.slick

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject
import javax.inject.Singleton
import models.dao.ShortOptionDAO
import models.dao.slick.table.ShortOptionTable
import play.api.db.slick.DatabaseConfigProvider

@Singleton
class SlickShortOptionDAO @Inject() (val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends ShortOptionDAO with ShortOptionTable with SlickCommontDAO {

  import dbConfig.profile.api._
  import scala.concurrent.Future.{ successful => future }

  private val queryByName = Compiled(
    (name: Rep[String]) => table.filter(_.name === name))

  override def getShortOptions(): Future[Seq[models.ShortOption]] =
    db.run(table.result)

  override def getShortOptionByName(name: String): Future[Option[models.ShortOption]] =
    db.run(queryByName(name).result.headOption)

  override def updateShortOptionByName(name: String, value: String): Future[Option[models.ShortOption]] =
    db.run(table.filter(_.name === name).map(_.value).update(value).map(_ > 1)
      .flatMap(_ => queryByName(name).result.headOption))

  override def close: Future[Unit] =
    future(db.close())

}
