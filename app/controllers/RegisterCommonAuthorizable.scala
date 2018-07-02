package controllers

import java.io.IOException

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import com.sendgrid.Content
import com.sendgrid.Email
import com.sendgrid.Mail
import com.sendgrid.Method
import com.sendgrid.SendGrid

import javax.inject.Inject
import models.dao.AccountDAO
import models.dao.SessionDAO
import play.Logger
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents
import play.api.mvc.Result

class RegisterCommonAuthorizable @Inject() (
  cc: ControllerComponents,
  accountDAO: AccountDAO,
  sessionDAO: SessionDAO,
  config: Configuration)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport with LoggerSupport {

  protected def createAccount(
    emailPatternName: String,
    login: String,
    email: String)(
    f: models.Account => Result): Future[Result] = {
    accountDAO.createAccountOptWithClientRole(
      login,
      email) map (_.fold(BadRequest("Can't create account")) { account =>
      val from = new Email(config get[String] "sendgrid.from")
      val subject = config get[String] "sendgrid.subject"
      val to = new Email(account.email)
      val content = new Content(
        "text/plain",
        (config get[String] emailPatternName)
          .replace("%account.login%", account.login)
          .replace("%account.confirmCode%", account.confirmCode.get))
      val mail = new Mail(from, subject, to, content)
      val sg = new SendGrid(config get[String] "sendgrid.apikey");
      val request = new com.sendgrid.Request()
      try {
        request.setMethod(Method.POST)
        request.setEndpoint("mail/send")
        request.setBody(mail.build())
        val response = sg.api(request)
        if (response.getStatusCode() == 202)
          f(account)
        else {
          Logger.error("can't send email")
          Logger.error("status code: " + response.getStatusCode())
          Logger.error("body: " + response.getBody())
          Logger.error("headers: " + response.getHeaders())
          BadRequest("Some problems")
        }
      } catch {
        case e: IOException =>
          Logger.error(e.toString())
          BadRequest("Some problems")
      }
    })
  }

}

