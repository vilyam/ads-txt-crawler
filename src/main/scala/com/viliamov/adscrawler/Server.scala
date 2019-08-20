package com.viliamov.adscrawler

import com.google.inject.Guice
import com.viliamov.adscrawler.guice.{CommonModule, ParserModule, WebModule}

object Server extends App {
  val injector = Guice.createInjector(
    new CommonModule(),
    new ParserModule(),
    new WebModule())
}
