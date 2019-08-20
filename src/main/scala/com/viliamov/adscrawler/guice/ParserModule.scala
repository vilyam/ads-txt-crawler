package com.viliamov.adscrawler.guice

import com.google.inject.AbstractModule
import com.viliamov.adscrawler.service.CrawlerService

class ParserModule extends AbstractModule {
  override def configure() = {
    bind(classOf[CrawlerService]).asEagerSingleton()
  }
}
