package models

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
