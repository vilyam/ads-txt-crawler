package com.viliamov.adscrawler.service

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.google.common.net.HttpHeaders
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.viliamov.adscrawler.dao.AdsDao
import javax.inject.Inject
import org.apache.commons.validator.routines.UrlValidator

import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._
import scala.compat.java8.OptionConverters._
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}


class CrawlerService @Inject()(implicit
                               config: Config,
                               akka: ActorSystem,
                               materializer: ActorMaterializer,
                               parserService: AdRecordParserService,
                               adsDao: AdsDao) extends LazyLogging {

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

    fileUris.foreach(uri => httpCall(getPublisherName(uri), uri))
  }

  def getPublisherName(uri: Uri): String = uri.authority.host.address()

  def httpCall(publisherName: String, uri: Uri): Unit = {
    val request = HttpRequest(uri = uri)

    logger.debug(s"Make Http call to $uri")

    Http()
      .singleRequest(request)
      .onComplete {
        case Success(res) => res match {

          case resp@HttpResponse(StatusCodes.Redirection(_), headers, _, _) =>
            logger.debug(s"Redirected with ${resp.status} code")

            resp
              .getHeader(HttpHeaders.LOCATION).asScala
              .map(head => Uri(head.value()))
              .foreach(uri => httpCall(publisherName, uri))

          case HttpResponse(_, _, entity, _) =>
            Unmarshal(entity).to[String]
              .onComplete {
                case Success(str) => processResult(publisherName, str)
                case Failure(_) => logger.error(s"$publisherName: something wrong")
              }
        }
        case Failure(_) => logger.error(s"$publisherName: something wrong")
      }
  }

  def processResult(publisherName: String, res: String): Unit = {
    val records = parserService.process(publisherName, res)
    adsDao.put(publisherName, records)
  }
}
