package com.viliamov.adscrawler.service

import akka.http.scaladsl.model.Uri
import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}
import org.apache.commons.validator.routines.UrlValidator

import scala.jdk.CollectionConverters._

@Singleton
class UrlSourceService @Inject()(config: Config) {

  private val urlValidator = UrlValidator.getInstance()

  private val adsFilePath = Uri.Path.apply("/ads.txt")

  private val domains: Seq[String] = config
    .getStringList("crawler.list")
    .asScala.toSeq

  private val fileUris: Seq[Uri] = domains
    .filter(urlValidator.isValid)
    .map(Uri.apply)
    .map(_.withPath(adsFilePath))

  def getUris: Seq[Uri] = fileUris
}
