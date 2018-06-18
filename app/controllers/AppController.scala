package controllers

import scala.concurrent.ExecutionContext

import javax.inject.Inject
import javax.inject.Singleton
import models.daos.DAO
import play.api.i18n.I18nSupport
import play.api.mvc.AbstractController
import play.api.mvc.AnyContent
import play.api.mvc.ControllerComponents
import play.api.mvc.Request
import com.typesafe.config.Config
import play.api.i18n.Langs

@Singleton
class AppController @Inject() (
    cc: ControllerComponents, 
    dao: DAO, 
    config: Config)(implicit ec: ExecutionContext)
  extends Authorizable(cc, dao, config) {

  import scala.concurrent.Future.{ successful => future }

  def index() = Action.async { implicit request =>
    implicit val ac = new AppContext()
    optionalAuthorized { accountOpt =>
      future(Ok(views.html.app.index()))
    }
  }

}
