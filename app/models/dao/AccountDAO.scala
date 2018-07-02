package models.dao

import scala.concurrent.Future

import javax.inject.Inject
import models.Account
import play.api.inject.ApplicationLifecycle
import models.AccountStatus

trait AccountDAO {

  def findAccountOptById(id: Long): Future[Option[Account]]

  def findAccountOptByLogin(login: String): Future[Option[Account]]

  def findAccountOptByLoginOrEmail(loginOrEmail: String): Future[Option[Account]]

  def findAccountOptWithRolesByLoginOrEmail(loginOrEmail: String): Future[Option[Account]]

  def findAccountOptBySessionKeyAndIPWithRoles(sessionKey: String, ip: String): Future[Option[Account]]

  def findAccountOptByConfirmCodeAndLogin(login: String, code: String): Future[Option[Account]]

  def createAccountOptWithClientRole(login: String, email: String): Future[Option[Account]]

  def isLoginExists(login: String): Future[Boolean]

  def isEmailExists(email: String): Future[Boolean]

  def emailVerified(login: String, code: String, approveData: String): Future[Option[Account]]

  def findAccountsFilteredByNamePagesCount(filterOpt: Option[String]): Future[Int]

  def findAccountsFilteredByNamePage(filterOpt: Option[String], pageId: Int): Future[Seq[Account]]

  def setAccountStatus(accountId: Long, status: AccountStatus.AccountStatus): Future[Boolean]

  def close: Future[Unit]

}

class AccountDAOCloseHook @Inject() (dao: AccountDAO, lifecycle: ApplicationLifecycle) {
  lifecycle.addStopHook { () =>
    Future.successful(dao.close)
  }
}