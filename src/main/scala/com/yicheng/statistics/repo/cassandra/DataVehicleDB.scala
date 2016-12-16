package com.yicheng.statistics.repo.cassandra

import java.util.Date

import com.datastax.driver.core.{Row, Statement}
import com.google.common.reflect.TypeToken
import com.yicheng.statistics.common.JsonMapper
import com.yicheng.statistics.repo.model.Data._

import scala.collection.JavaConversions._
import com.datastax.driver.core.querybuilder.{QueryBuilder => qb}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by yuer on 2016/12/16.
  */
object DataVehicleDB {
  lazy val table = "t_data_vehicle"

  import CassandraHelper._
  import CassandraDB._

  def fromRow(row: Row) = {
    DataVehicle(
      row.getInt("device_type"),
      row.getString("device_id"),
      row.getTimestamp("data_time"),
      Option(row.getString("pos_data")).map(s => JsonMapper.readValue(s, classOf[DataInfoPos])),
      Option(row.getString("vehicle_data")).map(s => JsonMapper.readValue(s, classOf[DataInfoVehicle])),
      Option(row.getMap("vehicle_status", TypeToken.of(classOf[Integer]),
        TypeToken.of(classOf[Integer]))).map(_.map{case (k,v) => k.toInt -> v.toInt}).map(mapAsScalaMap(_)),
      Option(row.getString("battery_vol_data")).map(s => JsonMapper.readValue(s, classOf[DataInfoBatteryVolList])),
      Option(row.getString("battery_temp_data")).map(s => JsonMapper.readValue(s, classOf[DataInfoBatteryTempList])),
      Option(row.getString("fuel_data")).map(s => JsonMapper.readValue(s, classOf[DataInfoFuelBattery])),
      Option(row.getString("electric_data")).map(s => JsonMapper.readValue(s, classOf[DataInfoElectricMotorList])),
      Option(row.getString("motor_data")).map(s => JsonMapper.readValue(s, classOf[DataInfoCarMotor])),
      Option(row.getString("extreme_data")).map(s => JsonMapper.readValue(s, classOf[DataInfoExtreme])),
      Option(row.getString("dcdc_data")).map(s => JsonMapper.readValue(s, classOf[DataInfoDcDc])),
      Option(row.getString("charge_data")).map(s => JsonMapper.readValue(s, classOf[DataInfoChargeStage]))
    )
  }

  def list(deviceType: Int, deviceId: String, start: Date, end: Date): Future[Seq[DataVehicle]] = {
    val query: Statement = qb.select()
      .from(keyspace, table)
      .where(qb.eq("device_id", deviceId))
      .and(qb.eq("device_type", deviceType))
      .and(qb.gte("data_time", start))
      .and(qb.lte("data_time", end))
      .orderBy(qb.desc("data_time"))
    executeAsync(query).asScala.map(_.map(fromRow).toSeq)
  }


}
