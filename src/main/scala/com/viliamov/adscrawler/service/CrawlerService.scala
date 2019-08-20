package com.viliamov.adscrawler.service

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.Uri
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.viliamov.adscrawler.actor.StartCrawlingCommand
import javax.inject.{Inject, Named}

import scala.collection.parallel.ParSeq
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

class CrawlerService @Inject()(implicit
                               config: Config,
                               uriSourceService: UrlSourceService,
                               akka: ActorSystem,
                               @Named("supervisor") supervisor: ActorRef,
                               materializer: ActorMaterializer) extends LazyLogging {

  implicit val executor: ExecutionContext = akka.dispatcher

  init()

  def init(): Unit = {
    val duration = Duration(config.getInt("crawler.interval"), TimeUnit.MINUTES)

    akka.scheduler.schedule(Duration.Zero, duration, new Runnable {
      override def run(): Unit = doCrawling()
    })

    logger.info(s"Started. Scheduler interval is $duration")
  }

  def doCrawling(): Unit = {
    ParSeq(uriSourceService.getUris)
      .foreach(seq =>
        seq.foreach(uri => {
          supervisor ! StartCrawlingCommand(getPublisherName(uri), uri)
        }))
  }

  def getPublisherName(uri: Uri): String = uri.authority.host.address()
}
