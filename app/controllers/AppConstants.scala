package controllers

object AppConstants {

  val APP_NAME = "Blockwit Web Framework"

  val VERSION = "0.1a"

  val BACKEND_NAME = APP_NAME + " " + VERSION

  val DEFAULT_PAGE_SIZE = 17

  val SESSION_EXPIRE_TYME: Long = 3 * TimeConstants.DAY

  val PWD_MIN_LENGTH: Long = 12

}
