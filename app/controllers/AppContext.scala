package controllers

import be.objectify.deadbolt.scala.AuthenticatedRequest
import be.objectify.deadbolt.scala.models.Subject
import models.Account
import play.api.mvc.AnyContent

case class AppContext(val authorizedOpt: Option[models.Account] = None)

object AppContextObj {

  def apply(subject: Option[Subject]): AppContext =
    new AppContext(subject.map(_.asInstanceOf[Account]))

}

object AuthRequestToAppContext {

  implicit def ac(implicit request: AuthenticatedRequest[AnyContent]) =
    AppContextObj(request.subject)

}
