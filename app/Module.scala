import com.google.inject.AbstractModule

import be.objectify.deadbolt.scala.cache.HandlerCache
import models.dao.AccountDAO
import models.dao.AccountDAOCloseHook
import models.dao.RoleDAO
import models.dao.RoleDAOCloseHook
import models.dao.SessionDAO
import models.dao.SessionDAOCloseHook
import models.dao.ShortOptionDAO
import models.dao.ShortOptionDAOCloseHook
import models.dao.slick.SlickAccountDAO
import models.dao.slick.SlickRoleDAO
import models.dao.slick.SlickSessionDAO
import models.dao.slick.SlickShortOptionDAO
import play.api.Configuration
import play.api.Environment
import security.BaseHandlerCache
import models.dao.slick.SlickTelegramAccountDAO
import models.dao.TelegramAccountDAO
import models.dao.TelegramAccountDAOCloseHook

class Module(
  environment: Environment,
  configuration: Configuration) extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[HandlerCache]).to(classOf[BaseHandlerCache])

    bind(classOf[AccountDAO]).to(classOf[SlickAccountDAO])
    bind(classOf[AccountDAOCloseHook]).asEagerSingleton()

    bind(classOf[RoleDAO]).to(classOf[SlickRoleDAO])
    bind(classOf[RoleDAOCloseHook]).asEagerSingleton()

    bind(classOf[SessionDAO]).to(classOf[SlickSessionDAO])
    bind(classOf[SessionDAOCloseHook]).asEagerSingleton()

    bind(classOf[ShortOptionDAO]).to(classOf[SlickShortOptionDAO])
    bind(classOf[ShortOptionDAOCloseHook]).asEagerSingleton()
    
    bind(classOf[TelegramAccountDAO]).to(classOf[SlickTelegramAccountDAO])
    bind(classOf[TelegramAccountDAOCloseHook]).asEagerSingleton()

  }

}
