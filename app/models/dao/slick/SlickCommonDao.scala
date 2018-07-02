package models.dao.slick

import scala.concurrent.ExecutionContext

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import controllers.AppConstants

trait SlickCommontDAO
  extends HasDatabaseConfigProvider[slick.jdbc.JdbcProfile] {

  val dbConfigProvider: DatabaseConfigProvider

  import dbConfig.profile.api._

  def pages(size: Int, pageSize: Int): Int = {
    if (size == 0) 0 else {
      val fSize = size / pageSize
      if (fSize * pageSize < size) fSize + 1 else fSize
    }
  }

  def pages(size: Int): Int = pages(size, AppConstants.DEFAULT_PAGE_SIZE)

  def maybeOptActionF[A, R](maybe: Option[A])(action: A => R)(implicit ex: ExecutionContext): DBIO[Option[R]] =
    maybe match {
      case Some(a) => DBIO.successful(Some(action(a)))
      case _ => DBIO.successful(Option.empty[R])
    }

  def isOpt[R](condition: Boolean)(f: DBIO[Option[R]]): DBIO[Option[R]] =
    if (condition) f else DBIO.successful(None)

  def maybeOptAction[A, R](maybe: Option[A])(action: A => DBIO[Option[R]])(implicit ex: ExecutionContext): DBIO[Option[R]] =
    maybe match {
      case Some(a) => action(a)
      case _ => DBIO.successful(Option.empty[R])
    }

  def maybeOptAction[A, R](maybe: DBIO[Option[A]])(action: A => R)(implicit ex: ExecutionContext): DBIO[Option[R]] =
    maybe.map(_.map(action))

  def maybeOptActionSeq[A, R, C](maybe: Option[A])(maybe2: A => DBIO[Option[R]])(action: R => C)(implicit ex: ExecutionContext): DBIO[Option[C]] =
    maybeOptAction(maybe) { a =>
      maybeOptAction(maybe2(a)) { r => action(r) }
    }

  def maybeOptSqlActionSeq[A, R, C](maybe: DBIO[Option[A]])(maybe2: A => DBIO[Option[R]])(action: R => C)(implicit ex: ExecutionContext): DBIO[Option[C]] =
    maybe.flatMap { a => maybeOptActionSeq(a)(maybe2)(action) }

}













