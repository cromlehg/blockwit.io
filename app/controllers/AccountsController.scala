package controllers

import java.util.Base64
import java.util.UUID

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Random

import org.mindrot.jbcrypt.BCrypt

import AuthRequestToAppContext.ac
import be.objectify.deadbolt.scala.DeadboltActions
import javax.inject.Inject
import javax.inject.Singleton
import models.Account
import models.ConfirmationStatus
import models.Role
import models.Session
import models.TelegramAccount
import models.dao.AccountDAO
import models.dao.SessionDAO
import models.dao.TelegramAccountDAO
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms.boolean
import play.api.data.Forms.email
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.optional
import play.api.data.Forms.text
import play.api.i18n.Messages
import play.api.mvc.ControllerComponents
import play.api.mvc.Flash
import play.api.mvc.Request
import play.api.mvc.Result
import play.twirl.api.Html

@Singleton
class AccountsController @Inject() (
  cc: ControllerComponents,
  deadbolt: DeadboltActions,
  accountDAO: AccountDAO,
  sessionDAO: SessionDAO,
  telegramAccountDAO: TelegramAccountDAO,
  config: Configuration)(implicit ec: ExecutionContext)
  extends RegisterCommonAuthorizable(cc, accountDAO, sessionDAO, config) {

  import scala.concurrent.Future.{ successful => future }

  case class ApproveData(
    val login: String,
    val pwd: String,
    val repwd: String,
    val understandNotRecover: Boolean,
    val haveSecured: Boolean,
    val code: String)

  val loginVerifying = nonEmptyText(3, 20).verifying("Must contain lowercase letters and digits only.", name => name.matches("[a-z0-9]{3,20}"))

  val approveForm = Form(
    mapping(
      "login" -> loginVerifying,
      "pwd" -> nonEmptyText(8, 80),
      "repwd" -> nonEmptyText(8, 80),
      "understandNotRecover" -> boolean.verifying(_ == true),
      "haveSecured" -> boolean.verifying(_ == true),
      "code" -> nonEmptyText)(ApproveData.apply)(ApproveData.unapply))

  case class AuthData(val email: String, val pass: String)

  sealed trait RegData {
    def email: String
    def login: String
  }

  case class RegDataUser(
    override val email: String,
    override val login: String) extends RegData

  case class RegDataCompany(
    override val email: String,
    override val login: String,
    val companyName: String) extends RegData

  val authForm = Form(
    mapping(
      "email" -> nonEmptyText(3, 50),
      "pass" -> nonEmptyText(8, 80))(AuthData.apply)(AuthData.unapply))

  val regFormUser = Form(
    mapping(
      "email" -> email,
      "login" -> loginVerifying)(RegDataUser.apply)(RegDataUser.unapply))

  case class SettingsData(val login: String, val email: String, val telegramLogin: Option[String])

  val settingsForm = Form(
    mapping(
      "login" -> nonEmptyText(3, 50),
      "email" -> nonEmptyText(4, 100),
      "telegramLogin" -> optional(text))(SettingsData.apply)(SettingsData.unapply))

  private def accessToAccount(login: String)(f: Account => Future[Result])(implicit ac: AppContext): Future[Result] =
    ac.authorizedOpt.fold(future(BadRequest("Actor account are empty!"))) { actor =>
      accountDAO.findAccountOptByLogin(login) flatMap {
        _.fold(future(BadRequest("Account not found " + login))) { account =>
          if (actor.isAdmin || actor.login == login) f(account) else future(BadRequest("You have not access!"))
        }
      }
    }

  def settings(login: String) = deadbolt.SubjectPresent()() { implicit request =>
    accessToAccount(login) { account =>
      future(Ok(views.html.app.profile.settings(
        ac.authorizedOpt.get,
        settingsForm.fill {
          SettingsData(
            account.login,
            account.email,
            account.telegramAccountOpt.map(_.login))
        })))
    }
  }

  def settingsProcess(login: String) = deadbolt.SubjectPresent()() { implicit request =>
    accessToAccount(login) { account =>
      settingsForm.bindFromRequest.fold(
        formWithErrors => future(BadRequest(views.html.app.profile.settings(account, formWithErrors))), {
          data =>
            data.telegramLogin.fold {
              telegramAccountDAO.tryToRemoveTelegramLogin(account.id).map { _ =>
                Redirect(controllers.routes.AccountsController.settings(login)).flashing("success" -> "Telegram login successfully removed!")
              }
            } { telLogin =>
              if (telLogin.trim.isEmpty) {
                telegramAccountDAO.tryToRemoveTelegramLogin(account.id).map { _ =>
                  Redirect(controllers.routes.AccountsController.settings(login)).flashing("success" -> "Telegram login successfully removed!")
                }
              } else {
                telegramAccountDAO.updateOrCreateTelegramLogin(TelegramAccount(account.id, telLogin)).map { _ =>
                  Redirect(controllers.routes.AccountsController.settings(login)).flashing("success" -> "Telegram login successfully updated!")
                }
              }
            }
        })
    }
  }

  def login = deadbolt.SubjectNotPresent()() { implicit request =>
    future(Ok(views.html.app.login(authForm)))
  }

  def logout = deadbolt.SubjectPresent()() { implicit request =>
    request.session.get(Session.TOKEN).fold(future(BadRequest("You shuld authorize before"))) { curSessionKey =>
      sessionDAO.invalidateSessionBySessionKeyAndIP(curSessionKey, request.remoteAddress) map { _ =>
        Redirect(controllers.routes.AccountsController.login).withNewSession
      }
    }
  }

  def processLogin = deadbolt.SubjectNotPresent()() { implicit request =>
    authForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.app.login(formWithErrors))), { authData =>
      authCheckBlock(authData.email, authData.pass) { msg =>
        val formWithErrors = authForm.fill(authData)
        future(BadRequest(views.html.app.login(formWithErrors)(Flash(formWithErrors.data) + ("error" -> msg), implicitly, implicitly)))
      } {
        case (account, session) =>
          future(Redirect(routes.AppController.index))
      }
    })
  }

  //FIXME: Only for owner???
  def profile(login: String) = deadbolt.WithAuthRequest()() { implicit request =>
    accountDAO.findAccountOptByLogin(login) flatMap {
      _.fold(future(BadRequest("Account with login " + login + " not found!"))) { a =>
        future(Ok(views.html.app.profile.profile(a)))
      }
    }
  }

  def processApproveRegister = deadbolt.SubjectNotPresent()() { implicit request =>
    approveForm.bindFromRequest.fold(
      formWithErrors => future(BadRequest(views.html.app.approveRegister(formWithErrors))), {
        approveData =>
          if (approveData.pwd == approveData.repwd)
            accountDAO.findAccountOptByConfirmCodeAndLogin(approveData.login, approveData.code) flatMap (_.fold(future(BadRequest("Login or confirm code not found"))) { account =>
              accountDAO.emailVerified(approveData.login, approveData.code, approveData.pwd) map (_.fold(BadRequest("Can't verify email")) { accountVerified =>
                Ok(views.html.app.registerFinished())
              })
            })
          else {
            val formWithErrors = approveForm.fill(approveData)
            future(Ok(views.html.app.approveRegister(formWithErrors)(Flash(formWithErrors.data) + ("error" -> "Passwords should be equals"), implicitly, implicitly)))
          }
      })
  }

  def approveRegister(login: String, code: String) = deadbolt.SubjectNotPresent()() { implicit request =>
    accountDAO.findAccountOptByConfirmCodeAndLogin(login, code) map (_.fold(BadRequest("Login or confirm code not found")) { account =>
      Ok(views.html.app.approveRegister(approveForm.fill(ApproveData(
        login,
        BCrypt.hashpw(login.toString + code + System.currentTimeMillis() + Random.nextDouble(), BCrypt.gensalt()),
        null,
        false,
        false,
        code))))
    })
  }

  private def baseRegisterChecks[T <: RegData](
    regForm: Form[T])(f1: (String, Form[_]) => Future[Result])(f2: Form[_] => Html)(f3: (T, String, String) => Future[Result])(implicit request: Request[_], ac: AppContext) = {
    regForm.bindFromRequest.fold(
      formWithErrors => future(BadRequest(f2(formWithErrors))), {
        userInRegister =>
          accountDAO.isLoginExists(userInRegister.login) flatMap { isLoginExists =>
            if (isLoginExists)
              f1("Login already exists!", regForm.fill(userInRegister))
            else
              accountDAO.isEmailExists(userInRegister.email) flatMap { isEmailExists =>
                if (isEmailExists)
                  f1("Email already exists!", regForm.fill(userInRegister))
                else
                  f3(userInRegister, userInRegister.login, userInRegister.email)
              }
          }
      })

  }

  def registerProcessUser() = deadbolt.SubjectNotPresent()() { implicit request =>

    def redirectWithError(msg: String, form: Form[_]) =
      future(Ok(views.html.app.registerUser(form)(Flash(form.data) + ("error" -> msg), implicitly, implicitly)))

    baseRegisterChecks(regFormUser)(redirectWithError)(t => views.html.app.registerUser(t)) { (target, login, email) =>
      createAccount("sendgrid.letter", login, email) { account =>
        Ok(views.html.app.registerProcess())
      }
    }

  }

  def registerUser = deadbolt.SubjectNotPresent()() { implicit request =>
    future(Ok(views.html.app.registerUser(regFormUser)))
  }

  def denied = deadbolt.WithAuthRequest()() { implicit request =>
    future(Forbidden(views.html.app.denied()))
  }

  def adminAccounts(pageId: Int, filterOpt: Option[String]) = deadbolt.Restrict(List(Array(Role.ADMIN)))() { implicit request =>
    if (filterOpt.isDefined && !filterOpt.get.matches("[a-z0-9]{1,}")) {
      future(request.headers.get("referer")
        .fold {
          Redirect(controllers.routes.AccountsController.adminAccounts(1, None))
            .flashing("error" -> "Search string must contains only a-b or 0-9 symbols!")
        } { url =>
          Redirect(url)
            .flashing("error" -> "Search string must contains only a-b or 0-9 symbols!")
        })
    } else {
      accountDAO.findAccountsFilteredByNamePagesCount(filterOpt) flatMap { pagesCount =>
        if (pageId > pagesCount) future(BadRequest("Page not found " + pageId)) else
          accountDAO.findAccountsFilteredByNamePage(filterOpt, pageId) map { accounts =>
            Ok(views.html.app.adminAccounts(
              ac.authorizedOpt.get,
              accounts,
              pageId,
              pagesCount,
              filterOpt))
          }
      }
    }
  }

  def setAccountStatus(accountId: Long, statusStr: String) = deadbolt.Restrict(List(Array(Role.ADMIN)))() { implicit request =>
    models.AccountStatus.valueOf(statusStr).fold(future(BadRequest("Wrong status " + statusStr))) { status =>
      accountDAO.setAccountStatus(accountId, status) map { success =>
        if (success)
          (request.headers.get("referer")
            .fold(Redirect(controllers.routes.AppController.index)) { url => Redirect(url) })
            .flashing("error" -> ("New status has been set for account with id  " + accountId))
        else
          (request.headers.get("referer")
            .fold(Redirect(controllers.routes.AppController.index)) { url => Redirect(url) })
            .flashing("error" -> ("Can't set new status for account with id " + accountId))
      }
    }
  }

  protected def authCheckBlock(loginOrEmail: String, pwd: String)(error: String => Future[Result])(success: (models.Account, models.Session) => Future[Result])(implicit request: Request[_]): Future[Result] =
    accountDAO.findAccountOptByLoginOrEmail(loginOrEmail) flatMap (_.fold(error(Messages("app.login.error"))) { account =>
      if (account.confirmationStatus == ConfirmationStatus.WAIT_CONFIRMATION)
        error("Email waiting for confirmation!")
      else if (account.accountStatus == models.AccountStatus.LOCKED)
        error("Account locked!")
      else
        account.hash.fold(error(Messages("app.login.error"))) { hash =>

          if (BCrypt.checkpw(pwd, hash)) {
            def createSession = {
              val expireTime = System.currentTimeMillis + AppConstants.SESSION_EXPIRE_TYME
              val sessionKey = UUID.randomUUID.toString + "-" + account.id

              sessionDAO.create(
                account.id,
                request.remoteAddress,
                sessionKey,
                System.currentTimeMillis,
                expireTime) flatMap (_.fold(future(BadRequest("Coludn't create session"))) { session =>
                  val token = new String(Base64.getEncoder.encode(sessionKey.getBytes))
                  success(account, session).map(_.withSession(Session.TOKEN -> token))
                })
            }

            request.session.get(Session.TOKEN).fold(createSession)(curSessionKey =>
              sessionDAO.findSessionByAccountIdSessionKeyAndIP(account.id, request.remoteAddress, curSessionKey)
                flatMap (_.fold(createSession)(session => error("You should logout before."))))

          } else error(Messages("app.login.error"))

        }

    })

}

