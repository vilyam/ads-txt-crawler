package com.viliamov.adscrawler.model

import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{Json, Writes}

class AdRecordWrites extends Writes[AdRecord] {
  def writes(record: AdRecord) = {
    val required: Array[(String, JsValueWrapper)] = Array(
      "domain" -> record.domain,
      "accountId" -> record.accountId,
      "accountType" -> record.accountType)

    val added: Option[Array[(String, JsValueWrapper)]] =
      record.authorityId.map(value => required :+ ("authorityId" -> value))

    val full: Array[(String, JsValueWrapper)] = added.getOrElse(required)

    Json.obj(full: _*)
  }
}