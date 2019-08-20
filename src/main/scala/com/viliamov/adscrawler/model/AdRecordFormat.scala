package com.viliamov.adscrawler.model

import play.api.libs.json.{Json, OFormat, OWrites, Reads}

object AdRecordFormat {
  implicit val format: OFormat[AdRecord] = Json.format[AdRecord]
  implicit val reads: Reads[AdRecord] = Json.reads[AdRecord]
  implicit val writes: OWrites[AdRecord] = Json.writes[AdRecord]
}