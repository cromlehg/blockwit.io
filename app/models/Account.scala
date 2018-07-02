package models

import java.util.Date

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime

import be.objectify.deadbolt.scala.models.Subject

case class Account(
  val id: Long,
  val login: String,
  val email: String,
  val hash: Option[String],
  val confirmationStatus: ConfirmationStatus.ConfirmationStatus,
  val accountStatus: AccountStatus.AccountStatus,
  val registered: Long,
  val confirmCode: Option[String],
  override val roles: List[be.objectify.deadbolt.scala.models.Role],
  val sessionOpt: Option[Session],
  val avatarOpt: Option[String],
  val telegramAccountOpt: Option[TelegramAccount]) extends Subject {

  override val identifier = login

  override val permissions = List.empty

  val isAdmin = roles.map(_.name).contains(Role.ADMIN)

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

object ConfirmationStatus extends Enumeration() {

  type ConfirmationStatus = Value

  val WAIT_CONFIRMATION = Value("wait confirmation")

  val CONFIRMED = Value("confirmed")

}

object AccountStatus extends Enumeration {

  type AccountStatus = Value

  val NORMAL = Value("normal")

  val LOCKED = Value("locked")

  def valueOf(name: String) = this.values.find(_.toString == name)

  def isAccountStatus(s: String) = values.exists(_.toString == s)

}

object Account {

  def apply(
    id: Long,
    login: String,
    email: String,
    hash: Option[String],
    confirmationStatus: ConfirmationStatus.ConfirmationStatus,
    accountStatus: AccountStatus.AccountStatus,
    registered: Long,
    confirmCode: Option[String],
    roles: List[be.objectify.deadbolt.scala.models.Role],
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
    confirmationStatus: ConfirmationStatus.ConfirmationStatus,
    accountStatus: AccountStatus.AccountStatus,
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
      List.empty[be.objectify.deadbolt.scala.models.Role],
      None,
      None,
      None)

}
