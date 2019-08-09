package com.viliamov.adscrawler

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.AbstractModule
import com.typesafe.config.{Config, ConfigFactory}

class CommonModule extends AbstractModule {
  override def configure() = {
    implicit val akka: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    bind(classOf[Config]).toInstance(ConfigFactory.load())

    bind(classOf[ActorSystem]).toInstance(akka)

    bind(classOf[ActorMaterializer]).toInstance(materializer)
  }
}
