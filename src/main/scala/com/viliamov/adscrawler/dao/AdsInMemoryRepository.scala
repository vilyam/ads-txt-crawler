package com.viliamov.adscrawler.dao

import com.viliamov.adscrawler.model.AdRecord
import javax.inject.Singleton

import scala.collection.mutable

@Singleton
object AdsInMemoryRepository extends AdsRepository {
  private val map: mutable.HashMap[String, Seq[AdRecord]] = mutable.HashMap[String, Seq[AdRecord]]()

  override def search(publisherName: String): Seq[AdRecord] = {
    Option(publisherName)
      .map(name => name.trim.toLowerCase)
      .filter(!_.isEmpty)
      .map(name => map.keys
        .filter(key => key.contains(name))
        .flatMap(key => map.get(key))
        .flatten.toSeq)
      .getOrElse(Seq.empty)
  }

  override def get(publisherName: String): Seq[AdRecord] = {
    map.getOrElse(publisherName, Seq.empty)
  }

  override def put(publisherName: String, records: Seq[AdRecord]): Unit = {
    map.put(publisherName, records)
  }
}