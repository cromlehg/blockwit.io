package security

import java.util.Base64

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import be.objectify.deadbolt.scala.AuthenticatedRequest
import be.objectify.deadbolt.scala.DynamicResourceHandler
import be.objectify.deadbolt.scala.models.Subject
import javax.inject.Inject
import javax.inject.Singleton
import play.api.mvc.Request
import models.Session

@Singleton
class BaseHandler @Inject() (authSupport: AuthSupport)(dynamicResourceHandler: Option[DynamicResourceHandler] = None)
  extends AbstractHandler(dynamicResourceHandler) {

  import scala.concurrent.Future.{ successful => future }

  override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] = {
    Future(dynamicResourceHandler.orElse(Some(new BaseDynamicResourceHandler())))
  }

  override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]] =
    request.subject match {
      case Some(subj) => future(Some(subj))
      case _ =>
        request.session.get(Session.TOKEN) match {
          case Some(token) =>
            val sessionKey = new String(Base64.getDecoder.decode(token))
            authSupport.getAccount(sessionKey, request.remoteAddress)
          case _ => future(None)
        }
    }

}
