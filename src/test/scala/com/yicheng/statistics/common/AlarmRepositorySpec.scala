package com.yicheng.statistics.common

import java.util.Date

import com.yicheng.statistics.repo.RTAModel.{BatteryAlarm, VehicleSpeed, VehicleTired}
import org.scalatest.{FlatSpec, Matchers}
import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by yuer on 2016/12/6.
  */
class AlarmRepositorySpec extends FlatSpec with Matchers {

  "Test add BatteryAlarm Data" should "" in {
    val batteryAlarm = BatteryAlarm(device_type = 11,device_id="11",createtime = new Date(),alarmtype = 10001,
      alarmstarttime = Some(new Date()),alarmendtime = Some(new Date()) ,startlon = Some(12.1) ,startlat =Some(12.1),
      endlon = Some(12.1),endlat = Some(12.1),analysegroupsid= Some(11)
    )
    val batteryAlarmResult = AlarmDB.addBatteryAlarm(batteryAlarm)
    val result = Await.result(batteryAlarmResult,5 seconds)
    assert(result == 1 )
  }

  "Test add vehicleTiredResult Data" should "" in {
    val vehicleTired = VehicleTired(device_type = 11,device_id="11",startdatetime=Some(new Date()),
      enddatetime = Some(new Date()),createdatetime = new Date(),startlon = Some(12.1) ,startlat =Some(12.1),
      endlon = Some(12.1),endlat = Some(12.1))
    val vehicleTiredResult = AlarmDB.addVehicleTired(vehicleTired)
    val result = Await.result(vehicleTiredResult,5 seconds)
    assert(result == 1)
  }

  "Test add VehicleSpeed Data" should "" in {
    val vehicleSpeed = VehicleSpeed(device_type = 11,device_id="11",startdatetime=Some(new Date()),
      enddatetime = Some(new Date()),createdatetime = new Date(),maxspeed = Some(800),minspeed = Some(400),
      averagespeed = Some(400),speedthreshold = Some(200),startlon = Some(12.1) ,startlat =Some(12.1),
      endlon = Some(12.1),endlat = Some(12.1)
    )
    val VehicleSpeedResult = AlarmDB.addVehicleSpeed(vehicleSpeed)
    val result = Await.result(VehicleSpeedResult,5 seconds)
    assert(result == 1)
  }

  "Test Select BaseAlarm Data" should "" in {
    val baseAlarm = AlarmDB.getFirstAlarmDataByType(1001)
    val result = Await.result(baseAlarm,5 seconds)
    assert(result.size > 0)
  }


}
