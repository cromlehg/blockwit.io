package tasks

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

import akka.actor.ActorSystem
import javax.inject.Inject

class BaseActorTask @Inject() (actorSystem: ActorSystem)(implicit executionContext: ExecutionContext) {

  actorSystem.scheduler.schedule(initialDelay = 1.minutes, interval = 100000000.milliseconds) {
  }

}