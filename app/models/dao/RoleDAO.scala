package models.dao

import scala.concurrent.Future

import javax.inject.Inject
import play.api.inject.ApplicationLifecycle
import models.Role

trait RoleDAO {

  def findRolesByAccountId(accountId: Long): Future[Seq[Role]]

  def close: Future[Unit]

}

class RoleDAOCloseHook @Inject() (dao: RoleDAO, lifecycle: ApplicationLifecycle) {
  lifecycle.addStopHook { () =>
    Future.successful(dao.close)
  }
}
