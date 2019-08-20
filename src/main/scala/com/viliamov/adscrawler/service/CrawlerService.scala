package com.viliamov.adscrawler.service

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.Uri
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import org.apache.commons.validator.routines.UrlValidator

import scala.collection.parallel.ParSeq
import scala.concurrent.{ExecutionContext, Future}
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

  val domains: Seq[String] = config
    .getStringList("crawler.list")
    .asScala.toSeq

  val fileUris: Seq[Uri] = domains
    .filter(urlValidator.isValid)
    .map(Uri.apply)
    .map(_.withPath(adsFilePath))

  akka.scheduler.schedule(Duration.Zero, duration, new Runnable {
    override def run(): Unit = doCrawling()
  })

  logger.info(s"Started. Scheduler interval is $duration")

  def doCrawling(): Unit = {
    ParSeq(fileUris)
      .foreach(seq =>
        seq.foreach(uri => {
          getOrCreateUriCallActor(uri)
            .foreach(actor => actor ! StartCrawlingCommand(getPublisherName(uri), uri))
        }))
  }

  def getOrCreateUriCallActor(uri: Uri): Future[ActorRef] = {
    val name = s"${uri.authority.host}-call"

    akka.actorSelection(s"user/$name")
      .resolveOne(Duration(1, TimeUnit.SECONDS))
      .recover { case _: Exception =>
        akka.actorOf(UriCallActor.props.withDispatcher("uri-call-dispatcher"), name)
      }
  }

  def getPublisherName(uri: Uri): String = uri.authority.host.address()
}
