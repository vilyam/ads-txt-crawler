package com.viliamov.adscrawler.guice

import com.google.inject.AbstractModule
import com.viliamov.adscrawler.web.WebServer

class WebModule extends AbstractModule {
  override def configure() = {
    bind(classOf[WebServer]).asEagerSingleton()
  }
}
