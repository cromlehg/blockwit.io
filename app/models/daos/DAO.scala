package models.daos

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Random

import org.mindrot.jbcrypt.BCrypt

import controllers.AppConstants
import javax.inject.Inject
import models.Account
import models.AccountStatus
import models.ConfirmationStatus
import models.Roles
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import models.TelegramAccount

/**
 *
 * Queries with SlickBUG should be replace leftJoin with for comprehesive. Bug:
 * "Unreachable reference to after resolving monadic joins"
 *
 */

// inject this
// conf: play.api.Configuration,
// and then get conf value
// conf.underlying.getString(Utils.meidaPath)
class DAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends DBTableDefinitions with HasDatabaseConfigProvider[slick.jdbc.JdbcProfile] {

  import profile.api._
  import scala.concurrent.Future.{ successful => future }

  val maxLikesView = 10

  val pageSize = 17

  def pages(size: Int): Int = pages(size, AppConstants.DEFAULT_PAGE_SIZE)

  def updateOrCreateTelegramLogin(telegramAccount: TelegramAccount): Future[Boolean] =
    db.run(telegramAccounts.insertOrUpdate(telegramAccount).transactionally).map(_ == 1)

  def tryToRemoveTelegramLogin(id: Long): Future[Boolean] =
    db.run(telegramAccounts.filter(_.accountId === id).delete.transactionally).map(_ == 1)

  def setAccountStatus(accountId: Long, status: Int) =
    db.run(accounts.filter(_.id === accountId).map(_.accountStatus).update(status).transactionally).map(_ == 1)

  def getUserProfileInfoByLogin(login: String): Future[Option[models.Account]] = {
    val query = for {
      account <- accounts.filter(_.login === login).result.head
      telegramAccountOpt <- telegramAccounts.filter(_.accountId === account.id).result.headOption
      roles <- roles.filter(_.userId === account.id).map(_.role).result
    } yield (account, telegramAccountOpt, roles)
    db.run(query) map {
      case (account, telegramAccountOpt, roles) =>
        Some(account.copy(
          roles = roles,
          telegramAccountOpt = telegramAccountOpt))
    }
  }

  def pages(size: Int, pageSize: Int): Int = {
    if (size == 0) 0 else {
      val fSize = size / pageSize
      if (fSize * pageSize < size) fSize + 1 else fSize
    }
  }

  def getAdminAccounts(filterOpt: Option[String], pageId: Int): Future[Seq[models.Account]] =
    filterOpt.fold {
      db.run(
        accounts
          .joinLeft(telegramAccounts).on(_.id === _.accountId)
          .sortBy(_._1.id.desc)
          .drop(if (pageId > 0) pageSize * (pageId - 1) else 0)
          .take(pageSize)
          .result) map (_ map {
          case (a, t) => a.copy(telegramAccountOpt = t)
        })
    } { filter =>
      db.run(
        accounts
          .filter(_.login like ("%" + filter + "%"))
          .joinLeft(telegramAccounts).on(_.id === _.accountId)
          .sortBy(_._1.id.desc)
          .drop(if (pageId > 0) pageSize * (pageId - 1) else 0)
          .take(pageSize)
          .result) map (_ map {
          case (a, t) => a.copy(telegramAccountOpt = t)
        })
    }

  def getAccounts(filterOpt: Option[String], pageId: Int): Future[Seq[models.Account]] =
    filterOpt.fold {
      db.run(accounts.sortBy(_.id.desc).drop(if (pageId > 0) pageSize * (pageId - 1) else 0).take(pageSize).result)
    } { filter =>
      db.run(accounts.filter(_.login like ("%" + filter + "%")).sortBy(_.id.desc).drop(if (pageId > 0) pageSize * (pageId - 1) else 0).take(pageSize).result)
    }

  def getAccountsPagesCount(filterOpt: Option[String]): Future[Int] =
    filterOpt.fold {
      db.run(accounts.length.result).map { r =>
        pages(r, pageSize.toInt)
      }
    } { filter =>
      db.run(accounts.filter(_.login like ("%" + filter + "%")).length.result).map { r =>
        pages(r, pageSize.toInt)
      }
    }

  def getShortOptions(): Future[Seq[models.ShortOption]] =
    db.run(shortOptions.result)

  def getShortOptionByName(name: String): Future[Option[models.ShortOption]] =
    db.run(shortOptions.filter(_.name === name).result.headOption)

  def updateShortOptionByName(name: String, value: String): Future[Option[models.ShortOption]] =
    db.run(shortOptions.filter(_.name === name).map(_.value).update(value).map(_ > 1)
      .flatMap(_ => shortOptions.filter(_.name === name).result.headOption))

  def findAccountBySessionKeyAndIPRoles(sessionKey: String, ip: String): Future[Option[models.Account]] = {
    val query = for {
      dbSession <- sessions.filter(t => t.sessionKey === sessionKey && t.ip === ip)
      dbAccount <- accounts.filter(_.id === dbSession.userId)
    } yield (dbAccount, dbSession)
    val result = db.run(query.result.headOption).map(_.map {
      case (dbAccount, dbSession) => dbAccount.copy(sessionOpt = Some(dbSession))
    })
    updateAccountWithRoles(result)
  }

  def getAccountsPage(pageId: Long): Future[Seq[models.Account]] =
    db.run(accounts.sortBy(_.id.desc).drop(if (pageId > 0) AppConstants.DEFAULT_PAGE_SIZE * (pageId - 1) else 0).take(AppConstants.DEFAULT_PAGE_SIZE).result)

  def getAccountsPages(): Future[Int] =
    db.run(accounts.size.result) map pages

  def findAccountById(id: Long) =
    getAccountFromQuery(accounts.filter(_.id === id))

  def findAccountByEmail(email: String): Future[Option[Account]] =
    getAccountFromQuery(accounts.filter(_.email === email))

  def findAccountByLoginOrEmail(loginOrElamil: String): Future[Option[Account]] =
    getAccountFromQuery(accounts.filter(u => u.login === loginOrElamil || u.email === loginOrElamil))

  def findAccountIdByLoginOrEmail(loginOrElamil: String): Future[Option[Long]] =
    getAccountFromQuery(accounts.filter(u => u.login === loginOrElamil || u.email === loginOrElamil)).map(_.map(_.id))

  def findAccountByLogin(login: String): Future[Option[Account]] =
    getAccountFromQuery(accounts.filter(_.login === login))

  def isLoginExists(login: String): Future[Boolean] =
    db.run(accounts.filter(t => t.login === login.trim.toLowerCase || t.email === login).exists.result)

  def isEmailExists(email: String): Future[Boolean] =
    db.run(accounts.filter(_.email === email.trim.toLowerCase).exists.result)

  def findSessionByAccountIdSessionKeyAndIP(userId: Long, ip: String, sessionKey: String): Future[Option[models.Session]] =
    getSessionFromQuery(sessions.filter(s => s.userId === userId && s.ip === ip && s.sessionKey === sessionKey))

  def findAccountBySessionKeyAndIPWithRoles(sessionKey: String, ip: String): Future[Option[models.Account]] = {
    val query = for {
      dbSession <- sessions.filter(t => t.sessionKey === sessionKey && t.ip === ip)
      dbAccount <- accounts.filter(_.id === dbSession.userId)
    } yield (dbAccount, dbSession)
    updateAccountWithRoles(db.run(query.result.headOption).map(_.map {
      case (dbAccount, dbSession) => dbAccount.copy(sessionOpt = Some(dbSession))
    }))
  }

  def findAccountBySUIDAndSessionId(sessionId: Long, sessionKey: String): Future[Option[Account]] = {
    val query = for {
      dbSession <- sessions.filter(t => t.id === sessionId && t.sessionKey === sessionKey)
      dbAccount <- accounts.filter(_.id === dbSession.userId)
    } yield (dbAccount, dbSession)
    db.run(query.result.headOption).map(_.map {
      case (dbAccount, dbSession) => dbAccount.copy(sessionOpt = Some(dbSession))
    })
  }

  def getAccountFromQuery(query: Query[(Accounts), (models.Account), Seq]): Future[Option[models.Account]] =
    db.run(query.result.headOption)

  def getSessionFromQuery(query: Query[(Sessions), (models.Session), Seq]): Future[Option[models.Session]] =
    db.run(query.result.headOption)

  def findAccountWithRolesById(userId: Long): Future[Option[Account]] =
    updateAccountWithRoles(findAccountById(userId))

  def updateAccountWithRoles(futureOptAccount: Future[Option[Account]]): Future[Option[Account]] =
    futureOptAccount flatMap {
      case Some(u) => findRolesByAccountId(u.id).map { r => Some(u.copy(roles = r)) }
      case None => future(None)
    }

  def findRolesByAccountId(userId: Long) =
    db.run(roles.filter(_.userId === userId).result).map(_.map(_.role))

  def invalidateSessionBySessionKeyAndIP(sessionKey: String, ip: String): Future[Boolean] =
    db.run(sessions.filter(t => t.sessionKey === sessionKey && t.ip === ip).map(_.expire).update(System.currentTimeMillis).transactionally) map (r => if (r == 1) true else false)

  def createSession(
    userId: Long,
    ip: String,
    sessionKey: String,
    created: Long,
    expire: Long): Future[Option[models.Session]] = {
    val query = for {
      dbSession <- (sessions returning sessions.map(_.id) into ((v, id) => v.copy(id = id))) += models.Session(
        0,
        userId,
        ip,
        sessionKey,
        created,
        expire)
    } yield dbSession
    db.run(query.transactionally) map { dbSession => Some(dbSession) }
  }

  def getAccountsPages(pageSize: Long) =
    db.run(accounts.length.result).map { r =>
      pages(r, pageSize.toInt)
    }

  def findAccountByConfirmCodeAndLogin(login: String, code: String): Future[Option[models.Account]] =
    getAccountFromQuery(accounts.filter(t => t.login === login && t.confirmCode === code))

  def emailVerified(login: String, code: String, password: String): Future[Option[Account]] =
    db.run(accounts.filter(t => t.login === login && t.confirmCode === code)
      .map(t => (t.confirmCode, t.confirmationStatus, t.hash))
      .update(None, ConfirmationStatus.CONFIRMED, Some(BCrypt.hashpw(password, BCrypt.gensalt())))).flatMap { raws =>
      if (raws == 1) findAccountByLogin(login) else Future.successful(None)
    }

  def createAccount(
    login: String,
    email: String): Future[Option[models.Account]] = {
    val timestamp = System.currentTimeMillis()
    val query = for {
      dbAccount <- (accounts returning accounts.map(_.id) into ((v, id) => v.copy(id = id))) += models.Account(
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
    db.run(query.transactionally) flatMap {
      case dbAccount =>
        addRolesToAccount(dbAccount.id, Roles.CLIENT) map (t => Some(dbAccount.copy(roles = Seq(models.Roles.CLIENT))))
    }
  }

  def addRolesToAccount(userId: Long, rolesIn: Int*): Future[Unit] =
    db.run(DBIO.seq(roles ++= rolesIn.map(r => models.Role(userId, r))).transactionally)

}


