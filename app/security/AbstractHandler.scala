package security

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import be.objectify.deadbolt.scala.AuthenticatedRequest
import be.objectify.deadbolt.scala.DeadboltHandler
import be.objectify.deadbolt.scala.DynamicResourceHandler
import play.api.mvc.Request
import play.api.mvc.Result
import play.api.mvc.Results

abstract class AbstractHandler(dynamicResourceHandler: Option[DynamicResourceHandler] = None) extends DeadboltHandler {

  override def beforeAuthCheck[A](request: Request[A]) = Future(None)

  override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] =
    Future(dynamicResourceHandler.orElse(Some(new BaseDynamicResourceHandler())))

  override def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result] =
    getSubject(request).map { maybeSubject =>
      maybeSubject match {
        case Some(account) =>
          request.headers.get("referer")
            .fold {
              Results.Redirect(controllers.routes.AccountsController.denied())
            } { url =>
              Results.Redirect(url).flashing("error" -> "You have no permission!")
            }
        case _ =>
          Results.Redirect(controllers.routes.AccountsController.login)
      }
    }

}