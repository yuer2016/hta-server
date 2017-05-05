package com.yicheng.statistics.repo.cassandra

import java.util.Date


import com.google.common.reflect.TypeToken
import com.datastax.driver.core.querybuilder.{QueryBuilder => qb}
import com.yicheng.statistics.common.JsonMapper
import com.yicheng.statistics.repo.model.Data.{DataInfoPos, DataMqtt}

import scala.collection.JavaConversions.{mapAsJavaMap, mapAsScalaMap}
import scala.concurrent.Future
import com.datastax.driver.core._
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by yuer on 2017/5/5.
  */
object DataMqttDB {
  import CassandraHelper._
  import CassandraDB._

  lazy val table = "t_data_mqtt"

  def fromRow(row: Row): DataMqtt = {
    DataMqtt(
      row.getInt("device_type"),
      row.getString("device_id"),
      row.getTimestamp("data_time"),
      row.getTimestamp("track_id").getTime,
      Option(row.getString("pos_data")).map(s => JsonMapper.readValue(s, classOf[DataInfoPos])),
      Option(row.getMap("obd_data", TypeToken.of(classOf[Integer]),
        TypeToken.of(classOf[java.lang.Float]))).map(_.map{case (k,v) => k.toInt -> v.toFloat}).map(mapAsScalaMap(_)),
      Option(row.getMap("track_data", TypeToken.of(classOf[Integer]),
        TypeToken.of(classOf[Integer]))).map(_.map{case (k,v) => k.toInt -> v.toInt}).map(mapAsScalaMap(_))
    )
  }

  def find(): Future[Seq[DataMqtt]] = {
    val query: Statement = qb.select("device_id", "device_type", "gather_time",
      "obd_data", "pos_data", "track_data", "track_id")
      .from(keyspace, "t_mqtt_data")
      .where(qb.eq("device_id", "2222"))
      .and(qb.eq("device_type", 1))

    executeAsync(query).asScala.map(_.map(fromRow).toSeq)
  }

  def list(deviceType: Int, deviceId: String, start: Date, end: Date): Future[Seq[DataMqtt]] = {
    val query: Statement = qb.select()
      .from(keyspace, table)
      .where(qb.eq("device_id", deviceId))
      .and(qb.eq("device_type", deviceType))
      .and(qb.gte("data_time", start))
      .and(qb.lte("data_time", end))
      .orderBy(qb.asc("data_time"))
    executeAsync(query).asScala.map(_.map(fromRow).toSeq)
  }

  def batchCreate(mqttDataSeq: Seq[DataMqtt]): Unit = {
    mqttDataSeq.foreach(create)
  }

  def create(dataMqtt: DataMqtt) = {
    val query: Statement = qb.insertInto(keyspace, table)
      .value("device_id", dataMqtt.device_id)
      .value("device_type", dataMqtt.device_type)
      .value("data_time", dataMqtt.data_time)
      .value("track_id", dataMqtt.track_id)
      .value("pos_data", dataMqtt.pos_data.map(o => JsonMapper.writeValueAsString(o)).orNull)
      .value("obd_data", dataMqtt.obd_data.map(mapAsJavaMap).orNull)
      .value("track_data", dataMqtt.track_data.map(mapAsJavaMap).orNull)
      .setConsistencyLevel(ConsistencyLevel.ANY)

    executeAsync(query).asScala
  }
}
