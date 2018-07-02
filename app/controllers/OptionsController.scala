package controllers

import scala.concurrent.ExecutionContext

import AuthRequestToAppContext.ac
import be.objectify.deadbolt.scala.DeadboltActions
import javax.inject.Inject
import javax.inject.Singleton
import models.Role
import models.dao.ShortOptionDAO
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents

@Singleton
class OptionsController @Inject() (
  cc: ControllerComponents,
  deadbolt: DeadboltActions,
  shortOptionDAO: ShortOptionDAO,
  config: Configuration)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport with JSONSupport with LoggerSupport {

  import scala.concurrent.Future.{ successful => future }

  def options = deadbolt.RoleBasedPermissions(Role.ADMIN)() { implicit request =>
    shortOptionDAO.getShortOptions map (t => Ok(views.html.app.options(ac.authorizedOpt.get, t)))
  }

  def switchBooleanOption = deadbolt.RoleBasedPermissions(Role.ADMIN)(parse.json) { implicit request =>
    fieldString("name")(name => shortOptionDAO.getShortOptionByName(name)
      .flatMap(_.fold(future(BadRequest("Not found"))) { option =>
        if (option.ttype != models.ShortOptions.TYPE_BOOLEAN) future(BadRequest("Option must be boolean to switch")) else
          shortOptionDAO.updateShortOptionByName(name, if (option.toBoolean) "false" else "true") map {
            _.fold(BadRequest("Can't update option"))(t => Ok(t.toBoolean.toString))
          }
      }))
  }

}

