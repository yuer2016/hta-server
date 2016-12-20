package com.yicheng.statistics.actor

import akka.actor.{Actor, ActorLogging}
import com.yicheng.statistics.common.AlarmDB
import com.yicheng.statistics.repo.RTAModel._

/**
  * Created by yuer on 2016/12/2.
  */
class SaveHtaData extends Actor with ActorLogging{
  override def receive: Receive = {
    case batteryAlarm:BatteryAlarm =>
      AlarmDB.addBatteryAlarm(batteryAlarm)
    case vehicleTired:VehicleTired =>
      AlarmDB.addVehicleTired(vehicleTired)
    case vehicleSpeed:VehicleSpeed =>
      AlarmDB.addVehicleSpeed(vehicleSpeed)
    case vehicleDrivingBehavior: VehicleDrivingBehavior =>
      AlarmDB.addVehicleDrivingBehavior(vehicleDrivingBehavior)
    case  vehicleMileage:VehicleMileage =>
      AlarmDB.addVehicleMileage(vehicleMileage)
    case areaInout:AreaInout =>
      AlarmDB.addAreaInout(areaInout)
  }
}
