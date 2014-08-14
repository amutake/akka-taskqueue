package me.amutake.taskqueue

import akka.actor._
import akka.cluster._
import akka.contrib.pattern._

object TaskQueue {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("taskqueue")
    val seedNodes = args.map(AddressFromURIString(_))
    val recep = system.actorOf(Props(classOf[Receptionist], seedNodes), name = "recep")
    val worker = system.actorOf(Props(classOf[Worker], recep), name = "worker")
    ClusterReceptionistExtension(system).registerService(recep)
    sys.addShutdownHook(system.shutdown)
  }
}
