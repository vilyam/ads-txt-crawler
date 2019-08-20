package com.viliamov.adscrawler.dao

import java.net.URI

import com.viliamov.adscrawler.model.AdRecord
import com.viliamov.adscrawler.model.AdRecordFormat._
import javax.inject.Singleton
import play.api.libs.json.Json
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

import scala.jdk.CollectionConverters._

@Singleton
object AdsRedisRepository {

  private val pool = getPool

  def search(name: String): Seq[AdRecord] =
    autoClose(pool.getResource) { conn =>
      val keys = conn.keys(s"*$name*").asScala.toSeq
      conn.sunion(keys: _*).asScala
        .map(mapRowToRecord)
        .toSeq
    }

  def get(publisherName: String): Seq[AdRecord] =
    autoClose(pool.getResource) { conn =>
      conn.smembers(publisherName).asScala
        .map(mapRowToRecord)
        .toSeq
    }

  def put(publisherName: String, records: Seq[AdRecord]): Unit = {
    val seq = records.map(r => Json.toJson(r).toString())

    autoClose(pool.getResource) { conn =>
      conn.sadd(publisherName, seq: _*)
    }
  }

  def getPool: JedisPool = {
    val redisURI = new URI(System.getenv("REDIS_URL"))

    val poolConfig = new JedisPoolConfig
    poolConfig.setMaxTotal(8)
    poolConfig.setMaxIdle(4)
    poolConfig.setMinIdle(1)
    poolConfig.setTestOnBorrow(true)
    poolConfig.setTestOnReturn(true)
    poolConfig.setTestWhileIdle(true)

    new JedisPool(poolConfig, redisURI)
  }

  def mapRowToRecord(row: String): AdRecord = Json.fromJson[AdRecord](Json.parse(row)).get

  def autoClose[A <: AutoCloseable, B](closeable: A)(fun: A ⇒ B): B = {
    try {
      fun(closeable)
    } finally {
      closeable.close()
    }
  }

}