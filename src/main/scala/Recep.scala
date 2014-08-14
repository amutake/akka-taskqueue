package me.amutake.taskqueue

import akka.actor._

class Recep(val listener: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case AddTask(task) => {
      listener ! WhoIsLeader
      context.become(ack(sender), discardOld = false)
    }
    case GetStatus(uuid) => {

    }
  }

  def ack(client: ActorRef, task: Task) = {
    case leaderAddr: Address => {
      val leader = context.actorSelection(leaderAddr.toString + "/user/listener")
      leader ! NewTask(task)
      client ! Ack(uuid)
      context.unbecome
    }
  }

  def status(client: ActorRef, task: Task) = {

  }
}
