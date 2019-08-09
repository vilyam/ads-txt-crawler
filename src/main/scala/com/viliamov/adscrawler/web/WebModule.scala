package com.viliamov.adscrawler.web

import com.google.inject.AbstractModule

class WebModule extends AbstractModule {
  override def configure() = {
    bind(classOf[WebServer]).asEagerSingleton()
  }
}
