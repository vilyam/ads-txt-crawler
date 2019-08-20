package com.viliamov.adscrawler.model

import com.viliamov.adscrawler.model
import play.api.libs.json.{Format, Json, Reads}

object AccountType extends Enumeration {
  type AccountType = Value
  val DIRECT = Value
  val RESELLER = Value

  def withNameOpt(s: String): Option[AccountType] = values.find(_.toString.equalsIgnoreCase(s))

  implicit val format: Format[model.AccountType.Value] = Json.formatEnum(this)
  implicit val reads: Reads[model.AccountType.Value] = Reads.enumNameReads(this)
}
