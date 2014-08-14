package me.amutake.taskqueue

import akka.actor._
import akka.cluster._
import akka.cluster.ClusterEvent._

class ClusterListener(val seedNodes: Array[Address], val queue: ActorRef) extends Actor with ActorLogging {

  val cluster = Cluster(context.system)
  var leader: Option[Address] = None

  override def preStart() = {
    if (seedNodes.length > 0) {
      log.info("Join: {}", seedNodes.toString)
      cluster.joinSeedNodes(seedNodes.to[collection.immutable.Seq])
    } else {
      log.info("Join: {}", cluster.selfAddress.toString)
      cluster.join(cluster.selfAddress)
    }
    cluster.subscribe(self, classOf[LeaderChanged])
    cluster.subscribe(self, classOf[MemberUp])
  }

  def receive = {
    case state: CurrentClusterState => {
      log.info("Current members: {}", state.members.mkString(", "))
    }
    case MemberUp(member) => {
      log.info("Member is up: {}", member.toString)
      if (leader == Some(self.address)) {
        queue ! TellQueue(member)
      }
    }
    case LeaderChanged(address) => {
      log.info("Leader changed: {}", address.map(_.toString).getOrElse("none"))
      leader = address
    }
    case WhoIsLeader => {
      leader
    }
    case NewTask
  }
}
