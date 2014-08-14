package me.amutake.taskqueue

import akka.actor._

class Worker(val recep: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case Leader(ref) => {
      ref ! PleaseTask
    }
    case task: Task => {
      task match {
        case Task1 => {
          log.info("Processing task1...")
          Thread.sleep(3000)
          log.info("Finish task1")
        }
        case Task2 => {
          log.info("Processing task2...")
          Thread.sleep(5000)
          log.info("Finish task2")
        }
      }
      recep ! WhoIsLeader(self)
    }
  }
}
