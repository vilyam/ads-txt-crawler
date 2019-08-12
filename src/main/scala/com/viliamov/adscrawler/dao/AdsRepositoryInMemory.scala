package com.viliamov.adscrawler.dao

import com.viliamov.adscrawler.model.AdRecord
import javax.inject.Singleton

import scala.collection.mutable

@Singleton
object AdsRepositoryInMemory extends AdsRepository {
  private val map: mutable.HashMap[String, Seq[AdRecord]] = mutable.HashMap[String, Seq[AdRecord]]()

  override def search(publisherName: String): Seq[AdRecord] = {
    map.keys
      .find(key => key.contains(publisherName))
      .map(key => get(key))
      .getOrElse(Seq.empty)
  }

  override def get(publisherName: String): Seq[AdRecord] = {
    map.getOrElse(publisherName, Seq.empty)
  }

  override def put(publisherName: String, records: Seq[AdRecord]): Unit = {
    map.put(publisherName, records)
  }
}