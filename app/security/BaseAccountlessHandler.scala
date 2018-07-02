package security

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import be.objectify.deadbolt.scala.AuthenticatedRequest
import be.objectify.deadbolt.scala.models.Subject
import javax.inject.Inject

import scala.concurrent.Future

import javax.inject.Inject
import javax.inject.Singleton
import models.Account
import models.dao.AccountDAO

@Singleton
class BaseAccountlessHandler extends AbstractHandler {

  override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]] =
    Future(None)

}