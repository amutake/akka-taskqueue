package me.amutake.taskqueue

import akka.actor._

class Worker(val recep: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case Leader(ref) => {
      ref ! PleaseTask
    }
    case SomeTask(task) => {
      task match {
        case "task1" => {
          log.info("Processing task1...")
          Thread.sleep(3000)
          log.info("Finish task1")
        }
        case "task2" => {
          log.info("Processing task2...")
          Thread.sleep(5000)
          log.info("Finish task2")
        }
      }
      recep ! WhoIsLeader(self)
    }
    case NoneTask => {
      log.info("task nothing...")
      Thread.sleep(1000)
      recep ! WhoIsLeader(self)
    }
  }
}
