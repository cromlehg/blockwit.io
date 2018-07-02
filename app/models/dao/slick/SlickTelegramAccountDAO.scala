package models.dao.slick

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject
import javax.inject.Singleton
import models.dao.TelegramAccountDAO
import models.dao.slick.table.TelergamAccountTable
import play.api.db.slick.DatabaseConfigProvider
import models.TelegramAccount

@Singleton
class SlickTelegramAccountDAO @Inject() (
  val dbConfigProvider: DatabaseConfigProvider,
  val accountDAO: SlickAccountDAO)(implicit ec: ExecutionContext)
  extends TelegramAccountDAO with TelergamAccountTable with SlickCommontDAO {

  import scala.concurrent.Future.{ successful => future }
  import dbConfig.profile.api._

  override def tryToRemoveTelegramLogin(accountId: Long): Future[Boolean] =
    db.run(table.filter(_.accountId === accountId).delete.transactionally).map(_ == 1)

  override def updateOrCreateTelegramLogin(telegramAccount: TelegramAccount): Future[Boolean] =
    db.run(table.insertOrUpdate(telegramAccount).transactionally).map(_ == 1)

  override def close: Future[Unit] =
    future(db.close())

}
