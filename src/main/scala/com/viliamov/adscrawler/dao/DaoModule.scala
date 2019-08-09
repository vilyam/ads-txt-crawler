package com.viliamov.adscrawler.dao

import com.google.inject.AbstractModule

class DaoModule extends AbstractModule {
  override def configure() = {
    bind(classOf[AdsDao]).to(classOf[AdsDaoInMemoryImpl])
  }
}

