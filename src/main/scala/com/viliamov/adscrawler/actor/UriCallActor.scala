package com.viliamov.adscrawler.actor

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.{Http, HttpExt}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.google.common.net.HttpHeaders

import scala.compat.java8.OptionConverters._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

case class StartCrawlingCommand(publisherName: String, uri: Uri)

object UriCallActor {
  val props: Props = Props[UriCallActor].withDispatcher("uri-call-dispatcher")
}

class UriCallActor extends Actor with ActorLogging {
  final implicit val system: ActorSystem = context.system
  final implicit val executor: ExecutionContext = context.system.dispatchers.lookup("uri-call-dispatcher")
  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  val http: HttpExt = Http(context.system)

  override def receive: Receive = {
    case StartCrawlingCommand(name, uri) => httpCall(name, uri)
  }

  def httpCall(publisherName: String, uri: Uri): Unit = {
    val request = HttpRequest(uri = uri)
    log.info(s"Make Http call to $uri")

    http
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
    val name = s"parser"

    context
      .child(name).getOrElse(context.actorOf(AdRecordParserActor.props, name))
      .tell(ParseAdCommand(publisherName, res), self)
  }
}
