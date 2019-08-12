package com.viliamov.adscrawler.service

import com.google.inject.AbstractModule

class ParserModule extends AbstractModule {
  override def configure() = {
    bind(classOf[CrawlerService]).asEagerSingleton()
  }
}
