package me.amutake.taskqueue

import akka.actor._
import akka.cluster._
import akka.contrib.pattern._

object TaskQueue {
  def main(args: Array[String]) = {
    val system = ActorSystem("taskqueue")
    val seedNodes = args.map(AddressFromURIString(_))
    val recep = system.actorOf(Props[Recep], name = "recep")
    val queue = system.actorOf(Props[Queue], name = "queue")
    val worker = system.actorOf(Props[Worker], name = "worker")
    val listener = systeml.actorOf(Props(classOf[ClusterListener], seedNodes), name = "listener")
    ClusterReceptionistExtension(system).registerService(recep)
    sys.addShutdownHook(system.shutdown)
  }
}
