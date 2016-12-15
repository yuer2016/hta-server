package com.yicheng.statistics.common

import java.util.Date

import com.yicheng.statistics.repo.RTAModel._
import slick.jdbc.GetResult

import scala.concurrent.Future

/**
  * Created by yuer on 2016/11/29.
  */
trait AlarmRepository {
  this: RTADBCompoent =>

  import driver.api._

  private def parseDate(date: Date): java.sql.Timestamp = {
    new java.sql.Timestamp(date.getTime)
  }
  implicit val getBaseAlarmResult = GetResult(u => BaseAlarm(u.<<, u.<<, u.<<, u.<<, u.<<, u.<<, u.<<?, u.<<?, u.<<?,
    u.<<, u.<<?, u.<<?, u.<<?, u.<<?, u.<<?,u.<<?,u.<<?,u.<<?))

  def getFirstAlarmDataBySource(alarmSource: Int):Future[Seq[BaseAlarm]] = {
    db.run(sql"""SELECT B.device_type,B.device_id,B.alarm_type,B.alarm_source,B.alarm_cnt,
             B.alarm_time,B.alarm_start,MAX(B.alarm_stop) AS alarm_stop ,
             B.alarm_level,B.alarm_data,B.latitude,B.longitude,B.height,B.speed,
             B.direction,MAX(B.speed) AS maxspeed, MIN(B.speed) AS minspeed,AVG(B.speed) AS averagespeed
             FROM basealarm B WHERE B.alarm_source = ${alarmSource}
             GROUP BY B.device_id , B.device_type, B.alarm_start """.as[BaseAlarm])
  }

  def getLastAlarmDataBySource(device_type:Int,device_id:String,alarm_start: Date, alarm_stop: Date, alarmSource: Int): Future[BaseAlarm] = {
    db.run(sql"""SELECT B.device_type,B.device_id,B.alarm_type,B.alarm_source,B.alarm_cnt,B.alarm_time,B.alarm_start,
             B.alarm_stop,B.alarm_level,B.alarm_data,B.latitude,B.longitude,B.height,B.speed,
             B.direction FROM basealarm B WHERE B.alarm_source = ${alarmSource}
             and B.alarm_start = ${alarm_start} and B.alarm_stop = ${alarm_stop}
             and B.device_type = device_type and B.device_id = device_id  """.as[BaseAlarm].head)
  }

  def getFirstAlarmDataByType(alarmType: Int):Future[Seq[BaseAlarm]] = {
    db.run(sql"""SELECT B.device_type,B.device_id,B.alarm_type,B.alarm_source,B.alarm_cnt,
             B.alarm_time,B.alarm_start,MAX(B.alarm_stop) AS alarm_stop ,
             B.alarm_level,B.alarm_data,B.latitude,B.longitude,B.height,B.speed,
             B.direction ,MAX(B.speed) AS maxspeed, MIN(B.speed) AS minspeed,AVG(B.speed) AS averagespeed
             FROM basealarm B WHERE B.alarm_type = ${alarmType}
             GROUP BY B.device_id , B.device_type, B.alarm_start """.as[BaseAlarm])
  }

  def getLastAlarmDataByType(device_type:Int,device_id:String,alarm_start: Date, alarm_stop: Date, alarmType: Int): Future[BaseAlarm] = {
    db.run(sql"""SELECT B.device_type,B.device_id,B.alarm_type,B.alarm_source,B.alarm_cnt,B.alarm_time,B.alarm_start,
             B.alarm_stop,B.alarm_level,B.alarm_data,B.latitude,B.longitude,B.height,B.speed,
             B.direction FROM basealarm B WHERE B.alarm_type = ${alarmType}
             and B.alarm_start = ${alarm_start} and B.alarm_stop = ${alarm_stop}
             and B.device_type = device_type and B.device_id = device_id """.as[BaseAlarm].head)
  }

  def addBatteryAlarm(batteryAlarm: BatteryAlarm):Future[Int] = {
    alarm.run(
      sqlu"""INSERT INTO bi_hta_battery_alarm(deviceID,deviceType,alarmstarttime,alarmendtime,startlon,startlat,endlon,endlat,startmileage,
             endmileage,createtime,alarmtype,alarmbeginvalue,alarmendvalue,alarmlevel,analysegroupsid,remark)
             VALUES (${batteryAlarm.device_id},${batteryAlarm.device_type},
            ${batteryAlarm.alarmstarttime},${batteryAlarm.alarmendtime},${batteryAlarm.startlon},${batteryAlarm.startlat},
            ${batteryAlarm.endlon},${batteryAlarm.endlat},${batteryAlarm.startmileage},${batteryAlarm.endmileage},
            ${batteryAlarm.createtime},${batteryAlarm.alarmtype},${batteryAlarm.alarmbeginvalue},${batteryAlarm.alarmendvalue},
            ${batteryAlarm.alarmlevel},${batteryAlarm.analysegroupsid},${batteryAlarm.remark}
            )""")
  }

  def addVehicleTired(vehicleTired: VehicleTired):Future[Int] = {
    alarm.run(
      sqlu"""INSERT INTO bi_iaa_vehicle_tired(deviceID,deviceType,startdatetime,enddatetime,mileagethreshold,timethreshold,
         totaltime,startlon,startlat,endlon,endlat,startmileage,endmileage,totalmileage,analysegroupsid,analyseconditions,
         createdatetime) VALUES (${vehicleTired.device_id},${vehicleTired.device_type},
          ${vehicleTired.startdatetime},${vehicleTired.enddatetime},${vehicleTired.mileagethreshold},${vehicleTired.timethreshold},
          ${vehicleTired.totaltime},${vehicleTired.startlon},${vehicleTired.startlat},${vehicleTired.endlon},${vehicleTired.endlat}
         ,${vehicleTired.startmileage},${vehicleTired.endmileage},${vehicleTired.totalmileage},${vehicleTired.analysegroupsid},
          ${vehicleTired.analyseconditions},${vehicleTired.createdatetime}
         )""")
  }

  def addVehicleSpeed(vehicleSpeed: VehicleSpeed):Future[Int] = {
    alarm.run(
      sqlu"""INSERT INTO bi_iaa_vehicle_speed(deviceID,deviceType,startdatetime,enddatetime,totaltime,maxspeed,minspeed,
          averagespeed,speedthreshold,speedtype,startlon,startlat,endlon,endlat,analysegroupsid,analyseconditions,
          createdatetime,totalmileage) VALUES (${vehicleSpeed.device_id},${vehicleSpeed.device_type},
          ${vehicleSpeed.startdatetime},${vehicleSpeed.enddatetime},${vehicleSpeed.totaltime},${vehicleSpeed.maxspeed},
          ${vehicleSpeed.minspeed},${vehicleSpeed.averagespeed},${vehicleSpeed.speedthreshold},${vehicleSpeed.speedtype},
          ${vehicleSpeed.startlon},${vehicleSpeed.startlat},${vehicleSpeed.endlon},${vehicleSpeed.endlat},
          ${vehicleSpeed.analysegroupsid},${vehicleSpeed.analyseconditions},${vehicleSpeed.createdatetime},
          ${vehicleSpeed.totalmileage}
        )""")
  }

  def addVehicleDrivingBehavior(vdb: VehicleDrivingBehavior):Future[Int] = {
    alarm.run(
      sqlu""" INSERT INTO bi_hta_vehicle_drivingbehavior(deviceID,deviceType,latitude,longitude,time,type,
            totalmileage,createtime) VALUES(${vdb.device_id},${vdb.device_type},${vdb.latitude},${vdb.longitude},
            ${vdb.time},${vdb.vdbtype},${vdb.totalmileage},${vdb.createdatetime}) """)
  }
}

object AlarmDB extends AlarmRepository with RTADBCompoent
