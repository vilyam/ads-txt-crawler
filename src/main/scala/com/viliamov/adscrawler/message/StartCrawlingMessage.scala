package com.viliamov.adscrawler.message

import akka.http.scaladsl.model.Uri

case class StartCrawlingMessage (publisherName: String, uri: Uri)