package security

import scala.concurrent.Future

import javax.inject.Inject
import javax.inject.Singleton
import models.Account
import models.dao.AccountDAO

@Singleton
class AuthSupport @Inject() (accountDAO: AccountDAO) {

  def getAccount(sessionId: String, ip: String): Future[Option[Account]] =
    accountDAO.findAccountOptBySessionKeyAndIPWithRoles(sessionId, ip)

}