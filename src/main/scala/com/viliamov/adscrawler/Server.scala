package com.viliamov.adscrawler

import com.google.inject.Guice
import com.viliamov.adscrawler.service.ParserModule
import com.viliamov.adscrawler.web.WebModule

object Server extends App {
  val injector = Guice.createInjector(
    new CommonModule(),
    new ParserModule(),
    new WebModule())
}
