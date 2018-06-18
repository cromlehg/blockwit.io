package models

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime

import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.JsObject
import controllers.AppConstants
import controllers.AppContext
import java.util.Date

case class Account(
  val id: Long,
  val login: String,
  val email: String,
  val hash: Option[String],
  val confirmationStatus: Int,
  val accountStatus: Int,
  val registered: Long,
  val confirmCode: Option[String],
  val roles: Seq[Int],
  val sessionOpt: Option[Session],
  val avatarOpt: Option[String], 
  val telegramAccountOpt: Option[TelegramAccount]) {

  val isAdmin = roles.contains(Roles.ADMIN)

  val ldt = new LocalDateTime(registered, DateTimeZone.UTC)

  val notAdmin = !isAdmin

  lazy val createdPrettyTime =
    controllers.TimeConstants.prettyTime.format(new Date(registered))

  override def equals(obj: Any) = obj match {
    case account: Account => account.email == email
    case _ => false
  }

  override def toString = email

  def getRegistered(zone: String): DateTime = getRegistered.toDateTime(DateTimeZone forID zone)

  def getRegistered: LocalDateTime = ldt

  def toJsonAuth(inJsObj: JsObject)(implicit ac: AppContext): JsObject = {
    var jsObj = inJsObj ++ Json.obj("email" -> email)
    jsObj = confirmCode.fold(jsObj) { t => jsObj ++ Json.obj("confirm_code" -> t) }
    jsObj
  }

  def toJson(implicit ac: AppContext): JsObject = {
    var jsObj = Json.obj(
      "id" -> id,
      "login" -> login,
      "account_status" -> AccountStatus.strById(accountStatus),
      "confirmation_status" -> ConfirmationStatus.strById(confirmationStatus),
      "registered" -> registered,
      "login" -> login)
    ac.authorizedOpt.fold(jsObj)(_ => toJsonAuth(jsObj))
  }

  def loginMatchedBy(filterOpt: Option[String]): String =
    filterOpt.fold(login) { filter =>
      val start = login.indexOf(filter)
      val end = start + filter.length;
      val s = "<strong>"
      val e = "</strong>"
      if (start == 0 && end == login.length) {
        s + login + e
      } else if (start == 0 && end != login.length) {
        s + login.substring(0, end) + e + login.substring(end, login.length)
      } else if (start != 0 && end == login.length) {
        login.substring(0, start) + s + login.substring(start, login.length) + e
      } else {
        login.substring(0, start) + s + login.substring(start, end) + e + login.substring(end, login.length)
      }
    }

}

object Account {

  def apply(
    id: Long,
    login: String,
    email: String,
    hash: Option[String],
    confirmationStatus: Int,
    accountStatus: Int,
    registered: Long,
    confirmCode: Option[String],
    roles: Seq[Int],
    sessionOpt: Option[Session],
    avatarOpt: Option[String],
    telegramAccountOpt: Option[TelegramAccount]): Account =
    new Account(
      id,
      login,
      email,
      hash,
      confirmationStatus,
      accountStatus,
      registered,
      confirmCode,
      roles,
      sessionOpt,
      avatarOpt,
      telegramAccountOpt)

  def apply(
    id: Long,
    login: String,
    email: String,
    hash: Option[String],
    confirmationStatus: Int,
    accountStatus: Int,
    registered: Long,
    confirmCode: Option[String]): Account =
    new Account(
      id,
      login,
      email,
      hash,
      confirmationStatus,
      accountStatus,
      registered,
      confirmCode,
      Seq.empty[Int],
      None,
      None,
      None)

}
