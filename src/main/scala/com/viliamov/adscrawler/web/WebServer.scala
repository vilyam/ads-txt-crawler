package com.viliamov.adscrawler.web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.viliamov.adscrawler.dao.AdsDao
import com.viliamov.adscrawler.model.AdRecordWrites
import javax.inject.Inject
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

class WebServer @Inject()(implicit config: Config,
                          akka: ActorSystem,
                          materializer: ActorMaterializer,
                          adsDao: AdsDao) extends LazyLogging {

  implicit val executor: ExecutionContext = akka.dispatcher
  implicit val adRecordWrites: AdRecordWrites = new AdRecordWrites

  private val host = config.getString("http.host")
  private val port = config.getInt("http.port")

  def route = concat(
    pathPrefix("ads") {
      concat(
        path(Remaining) { str =>
          val seq = adsDao.search(str)
          complete(HttpEntity(ContentTypes.`application/json`, Json.toJson(seq).toString()))
        }
      )
    }
  )

  Http().bindAndHandle(route, host, port)

  logger.info(s"Started. You could search Ads at http://$host:$port/ads/*publisher" )
}
