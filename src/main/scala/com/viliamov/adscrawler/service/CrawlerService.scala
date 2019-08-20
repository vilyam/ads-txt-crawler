package com.viliamov.adscrawler.service

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.Uri
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject

import scala.collection.parallel.ParSeq
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration

class CrawlerService @Inject()(implicit
                               config: Config,
                               uriSourceService: UrlSourceService,
                               akka: ActorSystem,
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
          val message = StartCrawlingCommand(getPublisherName(uri), uri)

          getOrCreateUriCallActor(uri).foreach(actor => actor ! message)
        }))
  }

  def getOrCreateUriCallActor(uri: Uri): Future[ActorRef] = {
    val name = s"${uri.authority.host}-caller"

    akka.actorSelection(s"user/$name")
      .resolveOne(Duration(1, TimeUnit.SECONDS))
      .recover { case _: Exception =>
        akka.actorOf(UriCallActor.props, name)
      }
  }

  def getPublisherName(uri: Uri): String = uri.authority.host.address()
}
