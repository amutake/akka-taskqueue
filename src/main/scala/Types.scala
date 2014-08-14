package me.amutake.taskqueue

sealed trait Task
case object Task1 extends Task
case object Task2 extends Task

sealed trait API
case class AddTask(task: Task) extends API
case class GetStatus(uuid: String) extends API

sealed trait QueueControl
case class InitQueue(queue: collection.mutable.Queue[Task]) extends QueueControl
case class Enqueue(task: Task) extends QueueControl
case object Dequeue extends QueueControl
case class TellQueue(addr: Address) extends QueueControl

sealed trait

case class Ack(uuid: String)