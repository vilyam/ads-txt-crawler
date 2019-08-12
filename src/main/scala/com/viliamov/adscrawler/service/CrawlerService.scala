package com.viliamov.adscrawler.service

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.Uri
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.viliamov.adscrawler.message.StartCrawlingMessage
import javax.inject.Inject
import org.apache.commons.validator.routines.UrlValidator

import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._
import scala.concurrent.duration.Duration


class CrawlerService @Inject()(implicit
                               config: Config,
                               akka: ActorSystem,
                               materializer: ActorMaterializer) extends LazyLogging {

  implicit val executor: ExecutionContext = akka.dispatcher

  private val urlValidator = UrlValidator.getInstance()
  val adsFilePath = Uri.Path.apply("/ads.txt")
  val duration = Duration(config.getInt("crawler.interval"), TimeUnit.MINUTES)

  akka.scheduler.schedule(Duration.Zero, duration, new Runnable {
    override def run(): Unit = doCrawling()
  })

  logger.info(s"Started. Scheduler interval is $duration")

  def doCrawling(): Unit = {
    val domains = config.getStringList("crawler.list").asScala

    val fileUris = domains
      .filter(urlValidator.isValid)
      .map(Uri.apply)
      .map(_.withPath(adsFilePath))

    val actor = akka.actorOf(Props[UriCallActor], name = "uri-call")
    fileUris.foreach(uri => actor ! StartCrawlingMessage(getPublisherName(uri), uri))
  }

  def getPublisherName(uri: Uri): String = uri.authority.host.address()
}
