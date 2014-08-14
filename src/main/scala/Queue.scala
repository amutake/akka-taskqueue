package me.amutake.taskqueue

class Queue extends Actor with ActorLogging {
  var queue = collection.mutable.Queue[Task]()
  def receive = {
    case InitQueue(q) => {
      queue = q
    }
    case Enqueue(task) => {
      queue += task
    }
    case Dequeue => {
      try {
        val task = queue.dequeue
        sender ! Some(task)
      } catch {
        case _: NoSuchElementException => {
          sender ! None
        }
      }
    }
    case TellQueue(addr) => {
      val act = context.actorSelection(addr.toString + "/user/queue")
      act ! InitQueue(queue)
    }
  }
}
