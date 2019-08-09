package com.viliamov.adscrawler.dao

import com.viliamov.adscrawler.model.AdRecord

trait AdsDao {
  def search(publisherName: String): Seq[AdRecord]

  def get(publisherName: String): Seq[AdRecord]

  def put(publisherName: String, records: Seq[AdRecord])
}
