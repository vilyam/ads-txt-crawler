package com.viliamov.adscrawler.service

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.{Http, HttpExt}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.google.common.net.HttpHeaders
import com.viliamov.adscrawler.message.{ParseAdMessage, StartCrawlingMessage}

import scala.compat.java8.OptionConverters._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class UriCallActor extends Actor with ActorLogging {
  final implicit val system: ActorSystem = context.system
  final implicit val executor: ExecutionContext = context.dispatcher
  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  val http: HttpExt = Http(context.system)

  override def receive: Receive = {
    case StartCrawlingMessage(name, uri) => httpCall(name, uri)
  }

  def httpCall(publisherName: String, uri: Uri): Unit = {
    val request = HttpRequest(uri = uri)
    log.debug(s"Make Http call to $uri")

    Http()
      .singleRequest(request)
      .onComplete {
        case Success(res) => res match {

          case resp@HttpResponse(StatusCodes.Redirection(_), _, _, _) =>
            log.debug(s"Redirected with ${resp.status} code")

            resp
              .getHeader(HttpHeaders.LOCATION).asScala
              .map(head => Uri(head.value()))
              .foreach(uri => httpCall(publisherName, uri))

          case HttpResponse(_, _, entity, _) =>
            Unmarshal(entity).to[String]
              .onComplete {
                case Success(str) => processResult(publisherName, str)
                case Failure(_) => log.error(s"$publisherName: something wrong")
              }
        }
        case Failure(_) => log.error(s"$publisherName: something wrong")
      }
  }

  def processResult(publisherName: String, res: String): Unit = {
    val message = ParseAdMessage(publisherName, res)
    val child = context.actorOf(Props[AdRecordParserActor], name = s"$publisherName-parser")
    child ! message
  }
}
