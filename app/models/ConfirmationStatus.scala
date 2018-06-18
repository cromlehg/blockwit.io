package models

object ConfirmationStatus {

  val CONFIRMED = 0

  val WAIT_CONFIRMATION = 1

  def idByStr(str: String): Option[Int] =
    str match {
      case "confirmed"          => Some(CONFIRMED)
      case "wait confirmation" => Some(WAIT_CONFIRMATION)
      case _                    => None
    }

  def strById(id: Int): Option[String] =
    id match {
      case 0 => Some("confirmed")
      case 1 => Some("wait confirmation")
      case _ => None
    }

}

