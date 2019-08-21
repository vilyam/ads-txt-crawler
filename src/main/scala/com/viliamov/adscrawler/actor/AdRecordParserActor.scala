package com.viliamov.adscrawler.actor

import akka.actor.{Actor, ActorLogging, Props}
import com.viliamov.adscrawler.actor.AdRecordParserActor.{parseLine, prepare}
import com.viliamov.adscrawler.dao.AdsInMemoryRepository
import com.viliamov.adscrawler.model.{AccountType, AdRecord}
import com.viliamov.adscrawler.service.AdRecordValidationService

case class ParseAdCommand(publisherName: String, raw: String)

object AdRecordParserActor {
  val props: Props = Props[AdRecordParserActor].withDispatcher("parser-dispatcher")

  def prepare(raw: String): Seq[String] = {
    raw
      .split("\n")
      .map(line => if (line.contains("#")) line.substring(0, line.indexOf("#")) else line)
      .map(_.trim)
      .filter(_.nonEmpty)
  }

  def parseLine(line: String): Either[String, AdRecord] = {
    line match {
      case s"$domain,$publisherId,$typeAcc,$authorityId" => construct(domain, publisherId, typeAcc, Some(authorityId))

      case s"$domain,$publisherId,$typeAcc" => construct(domain, publisherId, typeAcc)

      case _ => Left(s"Cannot parse '$line'")
    }
  }

  def construct(domain: String,
                accountId: String,
                accountType: String,
                authorityId: Option[String] = None
               ): Either[String, AdRecord] = {
    val domainVal = domain.trim
    val accountIdVal = accountId.trim
    val accountTypeVal = AccountType.withNameOpt(accountType.trim)
    val authorityIdVal = authorityId.map(_.trim)

    if (accountTypeVal.isEmpty) {
      println(accountType)
    }

    val validationResult = AdRecordValidationService.validate(domainVal, accountIdVal, accountTypeVal, authorityIdVal)

    if (validationResult.isEmpty) {
      Right(AdRecord(domainVal, accountIdVal, accountTypeVal.get, authorityIdVal))
    } else
      validationResult.get
  }
}

class AdRecordParserActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case ParseAdCommand(name, raw) =>
      val res: Seq[AdRecord] = process(name, raw)

      processResult(name, res)
  }

  def process(publisherName: String, raw: String): Seq[AdRecord] = {
    val (lefts, rights) = prepare(raw).iterator
      .map(parseLine)
      .toList
      .partitionMap(identity)

    lefts.foreach(line => log.debug(s"$publisherName: $line"))

    log.info(s"$publisherName: parsed ${rights.length} records with ${lefts.length} errors")

    rights
  }

  def processResult(publisherName: String, res: Seq[AdRecord]): Unit = {
    AdsInMemoryRepository.put(publisherName, res)
  }

}
