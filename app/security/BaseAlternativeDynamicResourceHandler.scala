package security

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import be.objectify.deadbolt.scala.AuthenticatedRequest
import be.objectify.deadbolt.scala.DeadboltHandler
import be.objectify.deadbolt.scala.DynamicResourceHandler

object BaseAlternativeDynamicResourceHandler extends DynamicResourceHandler {

  override def isAllowed[A](
    name: String,
    meta: Option[Any],
    handler: DeadboltHandler,
    request: AuthenticatedRequest[A]): Future[Boolean] =
    Future(false)

  override def checkPermission[A](
    permissionValue: String,
    meta: Option[Any],
    deadboltHandler: DeadboltHandler,
    request: AuthenticatedRequest[A]): Future[Boolean] =
    Future(false)

}