package models.dao.slick

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Random

import org.mindrot.jbcrypt.BCrypt

import controllers.AppConstants
import javax.inject.Inject
import javax.inject.Singleton
import models.Account
import models.AccountStatus
import models.ConfirmationStatus
import models.Role
import models.dao.AccountDAO
import models.dao.slick.table.AccountTable
import play.api.db.slick.DatabaseConfigProvider
import slick.sql.SqlAction
import models.RoleName

@Singleton
class SlickAccountDAO @Inject() (
  val dbConfigProvider: DatabaseConfigProvider,
  val roleDAO: SlickRoleDAO,
  val sessionDAO: SlickSessionDAO)(implicit ec: ExecutionContext)
  extends AccountDAO with AccountTable with SlickCommontDAO {

  import dbConfig.profile.api._
  import scala.concurrent.Future.{ successful => future }

  private val queryById = Compiled(
    (id: Rep[Long]) => table.filter(_.id === id))

  private val queryByLogin = Compiled(
    (login: Rep[String]) => table.filter(_.login === login))

  private val queryByEmail = Compiled(
    (login: Rep[String]) => table.filter(_.login === login))

  private val queryByLoginOrEmail = Compiled(
    (loginOrEmail: Rep[String]) => table.filter(t => t.login === loginOrEmail || t.email === loginOrEmail))

  private val queryByLoginAndConfirmCode = Compiled(
    (login: Rep[String], code: Rep[String]) => table.filter(t => t.login === login && t.confirmCode === code))

  def _findAccountOptByConfirmCodeAndLogin(login: String, code: String) =
    queryByLoginAndConfirmCode((login, code)).result.headOption

  def _findAccountOptById(id: Long): SqlAction[Option[Account], NoStream, Effect.Read] =
    queryById(id).result.headOption

  def _findAccountOptByLogin(login: String): SqlAction[Option[Account], NoStream, Effect.Read] =
    queryByLogin(login).result.headOption

  def _findAccountOptByLoginOrEmail(loginOrEmail: String): SqlAction[Option[Account], NoStream, Effect.Read] =
    queryByLoginOrEmail(loginOrEmail).result.headOption

  def _updateAccountWithRoles(account: Account) =
    roleDAO._findRolesByAccountId(account.id) map { t => Some(account.copy(roles = t.toList)) }

  def _isLoginExists(login: String) =
    queryByLogin(login).result.headOption.map(_.isDefined)

  def _isEmailExists(email: String) =
    queryByEmail(email).result.headOption.map(_.isDefined)

  def _findAccountOptWithRolesByLoginOrEmail(loginOrEmail: String) =
    _findAccountOptByLoginOrEmail(loginOrEmail) flatMap _updateAccountOptWithRoles

  def _findAccountOptWithRolesById(id: Long) =
    _findAccountOptById(id) flatMap _updateAccountOptWithRoles

  def _findAccountBySessionKeyAndIPWithRoles(sessionKey: String, ip: String) = {
    val query = for {
      sessionOpt <- sessionDAO._findSessionOptByKeyAndIp(sessionKey, ip)
      accountOpt <- maybeOptAction(sessionOpt)(t => _findAccountOptWithRolesById(t.userId))
    } yield (sessionOpt, accountOpt)

    query map {
      case (sessionOpt, accountOpt) => accountOpt.map(_.copy(sessionOpt = sessionOpt))
    }
  }

  def _createAccount(login: String, email: String) =
    for {
      dbAccount <- (table returning table.map(_.id) into ((v, id) => v.copy(id = id))) += models.Account(
        0,
        login,
        email,
        None,
        ConfirmationStatus.WAIT_CONFIRMATION,
        AccountStatus.NORMAL,
        System.currentTimeMillis,
        Some(BCrypt.hashpw(Random.nextString(5) + login + System.currentTimeMillis.toString, BCrypt.gensalt())
          .replaceAll("\\.", "s")
          .replaceAll("\\\\", "d")
          .replaceAll("\\$", "g").toList.map(_.toInt.toHexString).mkString.substring(0, 99)))
    } yield dbAccount

  def _createAccountWithClientRole(login: String, email: String) =
    _createAccount(login, email) flatMap { account =>
      roleDAO._addRolesToAccount(account.id, RoleName.CLIENT) map { _ =>
        Some(account.copy(roles = List(Role.roleClient(account.id))))
      }
    }

  def _updateAccountOptWithRoles(accountOpt: Option[Account]) =
    maybeOptAction(accountOpt)(_updateAccountWithRoles)

  override def createAccountOptWithClientRole(login: String, email: String): Future[Option[Account]] =
    db.run(_createAccountWithClientRole(login, email).transactionally)

  override def findAccountOptById(id: Long): Future[Option[Account]] =
    db.run(_findAccountOptById(id))

  override def findAccountOptBySessionKeyAndIPWithRoles(sessionKey: String, ip: String): Future[Option[Account]] =
    db.run(_findAccountBySessionKeyAndIPWithRoles(sessionKey, ip))

  override def findAccountOptByLogin(login: String): Future[Option[Account]] =
    db.run(_findAccountOptByLogin(login))

  override def findAccountOptByLoginOrEmail(loginOrEmail: String): Future[Option[Account]] =
    db.run(_findAccountOptByLoginOrEmail(loginOrEmail))

  override def findAccountOptWithRolesByLoginOrEmail(loginOrEmail: String): Future[Option[Account]] =
    db.run(_findAccountOptWithRolesByLoginOrEmail(loginOrEmail))

  override def findAccountOptByConfirmCodeAndLogin(login: String, code: String): Future[Option[Account]] =
    db.run(_findAccountOptByConfirmCodeAndLogin(login, code))

  override def isLoginExists(login: String): Future[Boolean] =
    db.run(_isLoginExists(login))

  override def isEmailExists(email: String): Future[Boolean] =
    db.run(_isEmailExists(email))

  override def findAccountsFilteredByNamePage(filterOpt: Option[String], pageId: Int): Future[Seq[models.Account]] =
    filterOpt.fold {
      db.run(
        table
          .sortBy(_.id.desc)
          .drop(if (pageId > 0) AppConstants.DEFAULT_PAGE_SIZE * (pageId - 1) else 0)
          .take(AppConstants.DEFAULT_PAGE_SIZE)
          .result)
    } { filter =>
      db.run(
        table
          .filter(_.login like ("%" + filter + "%"))
          .sortBy(_.id.desc)
          .drop(if (pageId > 0) AppConstants.DEFAULT_PAGE_SIZE * (pageId - 1) else 0)
          .take(AppConstants.DEFAULT_PAGE_SIZE)
          .result)
    }

  override def findAccountsFilteredByNamePagesCount(filterOpt: Option[String]): Future[Int] =
    filterOpt.fold {
      db.run(table.length.result).map { r =>
        pages(r)
      }
    } { filter =>
      db.run(table.filter(_.login like ("%" + filter + "%")).length.result).map { r =>
        pages(r)
      }
    }

  override def emailVerified(login: String, code: String, password: String): Future[Option[Account]] = {
    val query = for {
      isUpdated <- table.filter(t => t.login === login && t.confirmCode === code)
        .map(t => (t.confirmCode, t.confirmationStatus, t.hash))
        .update(None, ConfirmationStatus.CONFIRMED, Some(BCrypt.hashpw(password, BCrypt.gensalt())))
        .map(_ == 1)
      accountOpt <- isOpt(isUpdated)(_findAccountOptByLogin(login))
    } yield (accountOpt)

    db.run(query)
  }

  override def setAccountStatus(accountId: Long, status: AccountStatus.AccountStatus): Future[Boolean] =
    db.run(table
      .filter(_.id === accountId)
      .map(_.accountStatus)
      .update(status).transactionally).map(_ == 1)

  override def close: Future[Unit] =
    future(db.close())

}
