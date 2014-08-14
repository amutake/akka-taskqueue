package me.amutake.taskqueue.client

import akka.actor._
import akka.contrib.pattern._

object Client {
  def main(args: Array[String]) = {
    val system = ActorSystem("client")
    val initialContacts: Set[ActorSelection] = args.to[collection.immutable.Set].map { addr: String =>
      system.actorSelection(addr ++ "/user/receptionist")
    }
    val taskqueue = system.actorOf(ClusterClient.props(initialContacts))
    val sender = system.actorOf(Props(classOf[Sender], taskqueue))
    sender ! "go"
  }
}

class Sender(val taskqueue: ActorRef) extends Actor with ActorLogging {
  var one = true
  def receive = {
    case "go" => {
      if (one) {
        taskqueue ! ClusterClient.Send("/user/recep", "task1", localAffinity = true)
      } else {
        taskqueue ! ClusterClient.Send("/user/recep", "task2", localAffinity = true)
      }
      one = !one
    }
    case uuid: String => {
      log.info("uuid: {}", uuid)
      Thread.sleep(1000)
      if (one) {
        taskqueue ! ClusterClient.Send("/user/recep", "task1", localAffinity = true)
      } else {
        taskqueue ! ClusterClient.Send("/user/recep", "task2", localAffinity = true)
      }
      one = !one
    }
  }
}
