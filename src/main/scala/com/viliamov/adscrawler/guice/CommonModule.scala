package com.viliamov.adscrawler.guice

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import com.typesafe.config.{Config, ConfigFactory}
import com.viliamov.adscrawler.actor.Supervisor

class CommonModule extends AbstractModule {
  override def configure() = {
    implicit val akka: ActorSystem = ActorSystem("ads-crawler")
    val materializer: ActorMaterializer = ActorMaterializer()

    val supervisor: ActorRef = akka.actorOf(Supervisor.props(), "supervisor")

    bind(classOf[Config]).toInstance(ConfigFactory.load())

    bind(classOf[ActorSystem]).toInstance(akka)

    bind(classOf[ActorMaterializer]).toInstance(materializer)

    bind(classOf[ActorRef]).annotatedWith(Names.named("supervisor")).toInstance(supervisor)
  }
}
