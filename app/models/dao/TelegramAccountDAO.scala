package models.dao

import scala.concurrent.Future

import javax.inject.Inject
import models.TelegramAccount
import play.api.inject.ApplicationLifecycle

trait TelegramAccountDAO {

  def tryToRemoveTelegramLogin(accountId: Long): Future[Boolean]

  def updateOrCreateTelegramLogin(telegramAccount: TelegramAccount): Future[Boolean]

  def close: Future[Unit]

}

class TelegramAccountDAOCloseHook @Inject() (dao: TelegramAccountDAO, lifecycle: ApplicationLifecycle) {
  lifecycle.addStopHook { () =>
    Future.successful(dao.close)
  }
}