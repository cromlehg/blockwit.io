package models.dao

import scala.concurrent.Future

import javax.inject.Inject
import play.api.inject.ApplicationLifecycle

trait ShortOptionDAO {

  def getShortOptions(): Future[Seq[models.ShortOption]]

  def getShortOptionByName(name: String): Future[Option[models.ShortOption]]

  def updateShortOptionByName(name: String, value: String): Future[Option[models.ShortOption]]

  def close: Future[Unit]

}

class ShortOptionDAOCloseHook @Inject() (dao: ShortOptionDAO, lifecycle: ApplicationLifecycle) {
  lifecycle.addStopHook { () =>
    Future.successful(dao.close)
  }
}
