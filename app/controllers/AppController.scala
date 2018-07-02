package controllers

import scala.concurrent.ExecutionContext

import AuthRequestToAppContext.ac

import be.objectify.deadbolt.scala.DeadboltActions
import javax.inject.Inject
import javax.inject.Singleton
import models.dao.AccountDAO
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents

@Singleton
class AppController @Inject() (
  deadbolt: DeadboltActions,
  cc: ControllerComponents,
  accountDAO: AccountDAO, // <- Remove it after DB test success
  config: Configuration)(implicit ec: ExecutionContext)
  extends AbstractController(cc)
  with I18nSupport with LoggerSupport {

  import scala.concurrent.Future.{ successful => future }

  def index = deadbolt.WithAuthRequest()() { implicit request =>
    future(Ok(views.html.app.index()))
  }

}
