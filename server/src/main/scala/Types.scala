package me.amutake.taskqueue

import akka.actor._

// sealed trait Task
// case object Task1 extends Task
// case object Task2 extends Task

sealed trait API
case class AddTask(task: String) extends API
case class GetStatus(uuid: String) extends API

sealed trait QueueControl
case class InitQueue(queue: collection.mutable.Queue[String]) extends QueueControl
case class Enqueue(task: String) extends QueueControl
case object Dequeue extends QueueControl
// case class TellQueue(addr: Address) extends QueueControl

case class Ack(uuid: String)

case class WhoIsLeader(ret: ActorRef)
case class NewTask(task: String, uuid: String)
case object PleaseTask

case class Leader(ref: ActorSelection)

case class SomeTask(task: String)
case object NoneTask
