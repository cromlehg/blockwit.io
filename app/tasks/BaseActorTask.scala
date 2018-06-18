package tasks

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

import com.typesafe.config.Config

import akka.actor.ActorSystem
import javax.inject.Inject
import models.daos.DAO

class BaseActorTask @Inject() (actorSystem: ActorSystem, val dao: DAO, config: Config)(implicit executionContext: ExecutionContext) {

  actorSystem.scheduler.schedule(initialDelay = 1.minutes, interval = 100000000.milliseconds) {
  }

}