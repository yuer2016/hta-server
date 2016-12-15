package com.yicheng.statistics.actor

import java.util.Date

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, ActorRef, Props, ReceiveTimeout}
import com.yicheng.statistics.repo.RTAModel._
import com.yicheng.statistics.repo.model.AlarmUtils
import com.yicheng.statistics.service.HTAService
import play.api.libs.json.Json

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
/**
  * Created by yuer on 2016/11/29.
  */
class HtaAnalysis extends Actor with ActorLogging {

  val saveHtaDataActor:ActorRef = context.child("saveHtaData").
    getOrElse(context.actorOf(Props[SaveHtaData], "saveHtaData"))

  context.setReceiveTimeout(5 minutes)

  def receive: Receive = {
    case baseAlarmData: BaseAlarmData =>
      val nowDate = new Date
      val baseAlarm = baseAlarmData.baseAlarm
      baseAlarmData.alarmType match {
        case AlarmUtils.battery_alarm => //电池报警统计
          val batteryAlarm = conversion2BatteryAlarm(baseAlarm,nowDate)
          if (baseAlarm.alarm_start.getOrElse(nowDate) before baseAlarm.alarm_stop.getOrElse(nowDate)) {
            HTAService.getLastAlarmData(baseAlarm.device_type,baseAlarm.device_id,baseAlarm.alarm_start.get,
              baseAlarm.alarm_stop.get, baseAlarm.alarm_source) onComplete {
              case Success(lastAlarm) =>
                val json = Json.parse(lastAlarm.alarm_data)
                val current_mileage = (json \ "vehicle_data" \ "current_mileage").asOpt[Int]
                val begin_value = (json \ "vehicle_data" \ "battery_voltage").asOpt[String]
                saveHtaDataActor ! batteryAlarm.copy(endlat = lastAlarm.latitude,
                  endlon = lastAlarm.longitude,endmileage = current_mileage,alarmendvalue = begin_value)
              case Failure(error) => log.info(error.getMessage)
            }
          } else {
            saveHtaDataActor ! batteryAlarm
          }
        case AlarmUtils.vehicle_tired => //疲劳驾驶统计
          val vehicleTired = conversion2VehicleTired(baseAlarm,nowDate)
          if (baseAlarm.alarm_start.getOrElse(nowDate) before baseAlarm.alarm_stop.getOrElse(nowDate)) {
            HTAService.getLastAlarmData(baseAlarm.device_type,baseAlarm.device_id,baseAlarm.alarm_start.get,
              baseAlarm.alarm_stop.get, baseAlarm.alarm_type) onComplete {
              case Success(lastAlarm) =>
                val json = Json.parse(lastAlarm.alarm_data)
                val current_mileage = (json \ "vehicle_data" \ "current_mileage").asOpt[Int]
                val totalmileage = if (current_mileage.nonEmpty && vehicleTired.startmileage.nonEmpty){
                  Some(current_mileage.get - vehicleTired.startmileage.get)
                } else{
                  None
                }
                saveHtaDataActor !
                  vehicleTired.copy(endlat = lastAlarm.latitude,
                    endlon = lastAlarm.longitude, endmileage = current_mileage,totalmileage = totalmileage)
              case Failure(error) => log.info(error.getMessage)
            }
          } else {
            saveHtaDataActor ! vehicleTired
          }
        case AlarmUtils.vehicle_speed => //超速驾驶统计
          val vehicleSpeed = conversion2VehicleSpeed(baseAlarm,nowDate)
          if (baseAlarm.alarm_start.getOrElse(nowDate) before baseAlarm.alarm_stop.getOrElse(nowDate)) {
            HTAService.getLastAlarmData(baseAlarm.device_type,baseAlarm.device_id,baseAlarm.alarm_start.get,
              baseAlarm.alarm_stop.get, baseAlarm.alarm_type) onComplete {
              case Success(lastAlarm) =>
                println("发送消息:{}",lastAlarm)
                saveHtaDataActor ! vehicleSpeed.copy(endlat = lastAlarm.latitude, endlon = lastAlarm.longitude)
              case Failure(error) => log.info(error.getMessage)
            }
          } else {
            saveHtaDataActor ! vehicleSpeed
          }
          //驾驶行为统计信息
        case tpe:Int if(1102 until 1103) contains tpe  =>
          val vehicleDrivingBehavior = conversion2VehicleDrivingBehavior(baseAlarm,nowDate)
          saveHtaDataActor ! vehicleDrivingBehavior
      }
    case ReceiveTimeout =>
      context.parent !  Stop
  }

  def conversion2BatteryAlarm(baseAlarm:BaseAlarm,nowDate:Date): BatteryAlarm ={
    val json = Json.parse(baseAlarm.alarm_data)
    val param_id = (json \ "alarm_param" \ "param_id").asOpt[Int]
    val current_mileage = (json \ "vehicle_data" \ "current_mileage").asOpt[Int]
    val begin_value = (json \ "vehicle_data" \ "battery_voltage").asOpt[String]
    BatteryAlarm(device_type = baseAlarm.device_type, device_id = baseAlarm.device_id,
      alarmstarttime = baseAlarm.alarm_start, alarmendtime = baseAlarm.alarm_stop, startlat = baseAlarm.latitude,
      startlon = baseAlarm.longitude, endlat = baseAlarm.latitude, endlon = baseAlarm.longitude, createtime = nowDate,
      alarmtype = baseAlarm.alarm_type, alarmlevel = baseAlarm.alarm_level,analysegroupsid = param_id,
      alarmbeginvalue = begin_value,startmileage = current_mileage,endmileage = current_mileage )
  }

  def conversion2VehicleTired(baseAlarm:BaseAlarm,nowDate:Date): VehicleTired ={
    val json = Json.parse(baseAlarm.alarm_data)
    val param_id = (json \ "alarm_param" \ "param_id").asOpt[Int]
    val current_mileage = (json \ "vehicle_data" \ "current_mileage").asOpt[Int]
    val total_time = if(baseAlarm.alarm_stop.getOrElse(nowDate) after baseAlarm.alarm_start.getOrElse(nowDate)){
      ((baseAlarm.alarm_stop.getOrElse(nowDate).getTime -
        baseAlarm.alarm_start.getOrElse(nowDate).getTime) / 1000).toInt
    }else {
       0
    }
    VehicleTired(device_type = baseAlarm.device_type, device_id = baseAlarm.device_id,
      createdatetime = nowDate,startdatetime = baseAlarm.alarm_start,
      enddatetime = baseAlarm.alarm_stop,startlat = baseAlarm.latitude,
      startlon = baseAlarm.longitude, endlat = baseAlarm.latitude, endlon = baseAlarm.longitude
      ,analysegroupsid = param_id,startmileage = current_mileage ,totaltime = Some(total_time),
      endmileage = current_mileage, totalmileage = Some(0)
    )
  }

  def conversion2VehicleSpeed(baseAlarm:BaseAlarm,nowDate:Date): VehicleSpeed ={
    val json = Json.parse(baseAlarm.alarm_data)
    val param_id = (json \ "alarm_param" \ "param_id").asOpt[Int]
    val speed_threshold = (json \ "vehicle_data" \ "battery_voltage").asOpt[Int].getOrElse(0)
    val speed_type = (json \ "alarm_status").asOpt[Int].getOrElse(1) match {
      case 1 => "超高速报警"
      case 2 => "超低速报警"
    }
    val total_time = if(baseAlarm.alarm_stop.getOrElse(nowDate) after baseAlarm.alarm_start.getOrElse(nowDate)){
      ((baseAlarm.alarm_stop.getOrElse(nowDate).getTime -
        baseAlarm.alarm_start.getOrElse(nowDate).getTime) / 1000).toInt
    }else {
      0
    }
    VehicleSpeed(device_type = baseAlarm.device_type, device_id = baseAlarm.device_id,
      createdatetime = nowDate,startdatetime = baseAlarm.alarm_start,
      enddatetime = baseAlarm.alarm_stop,maxspeed = baseAlarm.maxspeed,minspeed = baseAlarm.minspeed,
      averagespeed = baseAlarm.averagespeed,speedthreshold = Some(speed_threshold),startlat = baseAlarm.latitude,
      startlon = baseAlarm.longitude, endlat = baseAlarm.latitude, endlon = baseAlarm.longitude,
      analysegroupsid = param_id,speedtype=Some(speed_type),totaltime = Some(total_time)
    )
  }

  def conversion2VehicleDrivingBehavior(baseAlarm:BaseAlarm,nowDate:Date):VehicleDrivingBehavior ={
    VehicleDrivingBehavior(device_id =baseAlarm.device_id,device_type = baseAlarm.device_type,
      longitude = baseAlarm.longitude.getOrElse(0.0),latitude = baseAlarm.latitude.getOrElse(0.0),
      time = baseAlarm.alarm_time, vdbtype = baseAlarm.alarm_type, createdatetime = nowDate)
  }

}
