package me.amutake.taskqueue

import akka.actor._
import akka.cluster._
import akka.cluster.ClusterEvent._
import akka.contrib.pattern._
import akka.contrib.pattern.DistributedPubSubMediator._

import java.util.UUID

class Receptionist(val seedNodes: Array[Address]) extends Actor with ActorLogging {

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSubExtension(context.system).mediator
  mediator ! Subscribe("queue", self)
  var leader: Option[ActorSelection] = None
  var queue = collection.mutable.Queue[String]()
  def isLeaderMe = leader == Some(self)

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
      if (leader == Some(self)) {
        val target = context.actorSelection(member.toString + "/user/recep")
        target ! InitQueue(queue)
      }
    }
    case LeaderChanged(address) => {
      log.info("Leader changed: {}", address.map(_.toString).getOrElse("none"))
      // IS THIS OK?
      if (isLeaderMe) {
        leader = address.map(a => context.actorSelection(a.toString + "/user/recep"))
        if (!isLeaderMe) {
          mediator ! Subscribe("queue", self)
        }
      } else {
        leader = address.map(a => context.actorSelection(a.toString + "/user/recep"))
        if (isLeaderMe) {
          mediator ! Unsubscribe("queue", self)
        }
      }
    }
    case WhoIsLeader(ret) => {
      leader.fold(self ! WhoIsLeader(ret)) { l =>
        log.info("Leader is this: {}", l.toString)
        ret ! Leader(l)
      }
    }
    // queue control
    case InitQueue(q) => {
      queue = q
    }
    case Enqueue(task) => {
      log.info("Enqueue!")
      queue += task
    }
    case Dequeue => {
      try {
        queue.dequeue
      } catch {
        case _: Throwable => {}
      }
    }
    // from client
    case task: String => {
      log.info("Added a task from client: {}", task)
      leader.fold {
        log.info("leader is none")
        self ! AddTask(task)
      } { l =>
        log.info("Add to leader: {}", l.toString)
        val uuid = UUID.randomUUID.toString
        l ! NewTask(task, uuid)
        sender ! uuid
      }
    }
    // leader only
    case NewTask(task, uuid) => {
      log.info("New Task!")
      queue += task
      mediator ! Publish("queue", Enqueue(task))
    }
    case PleaseTask => {
      try {
        val task = queue.dequeue
        mediator ! Publish("queue", Dequeue)
        sender ! SomeTask(task)
      } catch {
        case _: Throwable => {
          sender ! NoneTask
        }
      }
    }
  }
}
