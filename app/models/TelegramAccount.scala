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

case class TelegramAccount(
  val accountId: Long,
  val login: String,
  val accountOpt: Option[Account]) {

}

object TelegramAccount {

  def apply(
    accountId: Long,
    login: String,
    accountOpt: Option[Account]): TelegramAccount =
    new TelegramAccount(
      accountId,
      login,
      accountOpt)

  def apply(
    accountId: Long,
    login: String): TelegramAccount =
    new TelegramAccount(
      accountId,
      login,
      None)

}
