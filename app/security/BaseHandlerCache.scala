package security

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Random

import org.mindrot.jbcrypt.BCrypt

import javax.inject.Inject
import javax.inject.Singleton
import models.dao.AccountDAO
import models.dao.SessionDAO
import play.api.Configuration
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
import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.DeadboltHandler
import be.objectify.deadbolt.scala.HandlerKey

@Singleton
class BaseHandlerCache @Inject() (authSupport: AuthSupport) extends HandlerCache {
  
    val defaultHandler: DeadboltHandler = new BaseHandler(authSupport)()

    // HandlerKeys is an user-defined object, containing instances of a case class that extends HandlerKey  
    val handlers: Map[Any, DeadboltHandler] = Map(HandlerKeys.defaultHandler -> defaultHandler,
                                                  HandlerKeys.altHandler -> new BaseHandler(authSupport)(Some(BaseAlternativeDynamicResourceHandler)),
                                                  HandlerKeys.userlessHandler -> new BaseAccountlessHandler)

    // Get the default handler.
    override def apply(): DeadboltHandler = defaultHandler

    // Get a named handler
    override def apply(handlerKey: HandlerKey): DeadboltHandler = handlers(handlerKey)
}