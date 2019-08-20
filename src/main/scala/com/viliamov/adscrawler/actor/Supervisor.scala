package com.viliamov.adscrawler.actor

import akka.actor.{Actor, ActorLogging, Props}

object Supervisor {
  def props(): Props = Props[Supervisor].withDispatcher("uri-call-dispatcher")
}

class Supervisor extends Actor with ActorLogging {
  override def preStart(): Unit = log.info("Application started")

  override def postStop(): Unit = log.info("Application stopped")

  override def receive: Receive = {
    case command@StartCrawlingCommand(_, uri) =>
      val name = s"${uri.authority.host}-caller"

      context
        .child(name).getOrElse(context.actorOf(UriCallActor.props, name))
        .tell(command, self)
  }
}
