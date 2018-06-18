package controllers

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Random

import org.mindrot.jbcrypt.BCrypt

import com.typesafe.config.Config

import javax.inject.Inject
import javax.inject.Singleton
import models.daos.DAO
import play.api.data.Form
import play.api.data.Forms.boolean
import play.api.data.Forms.email
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.mvc.ControllerComponents
import play.api.mvc.Flash
import play.api.mvc.Request
import play.api.mvc.Result
import play.twirl.api.Html
import play.api.i18n.Langs

@Singleton
class OptionsController @Inject() (
    cc: ControllerComponents, 
    dao: DAO, 
    config: Config)(implicit ec: ExecutionContext)
  extends RegisterCommonAuthorizable(cc, dao, config) with JSONSupport {

  import scala.concurrent.Future.{ successful => future }

  def options = Action.async { implicit request =>
    implicit val ac = new AppContext()
    onlyAdmin(a => dao.getShortOptions map (t => Ok(views.html.app.options(a, t))))
  }

  def switchBooleanOption = Action.async(parse.json) { implicit request =>
    implicit val ac = new AppContext()
    onlyAdmin(a => fieldString("name")(name => dao.getShortOptionByName(name) flatMap (_.fold(future(BadRequest("Not found"))) { option =>
      if (option.ttype != models.ShortOptions.TYPE_BOOLEAN) future(BadRequest("Option must be boolean to switch")) else
        dao.updateShortOptionByName(name, if (option.toBoolean) "false" else "true") map {
          _.fold(BadRequest("Can't update option"))(t => Ok(t.toBoolean.toString))
        }
    })))
  }

}

