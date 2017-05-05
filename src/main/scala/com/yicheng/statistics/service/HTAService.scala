package com.yicheng.statistics.service

import java.util.Date

import com.yicheng.statistics.common.AlarmDB
import com.yicheng.statistics.repo.RTAModel.BaseAlarm
import com.yicheng.statistics.repo.model.AlarmUtils

import scala.concurrent.Future

/**
  * Created by yuer on 2016/12/2.
  */
object HTAService {

  def getFirstAlarmData(alarmType:Int):Future[Seq[BaseAlarm]] ={
      alarmType match {
        case AlarmUtils.battery_alarm =>
          AlarmDB.getFirstAlarmDataBySource
        case typ:Int if(1000 to 1999) contains typ =>
          AlarmDB.getFirstAlarmDataByType(typ)
        case typ:Int if(10000 to 29999) contains typ =>
          AlarmDB.getFirstAlarmDataByType(typ)
      }
  }

  def getLastAlarmData(device_type:Int,device_id:String,alarm_stop:Date,alarmType:Int):Future[BaseAlarm] ={
    alarmType match {
      case typ:Int if(1000 to 1999) contains typ =>
        AlarmDB.getLastAlarmDataByType(device_type:Int,device_id:String,alarm_stop,typ)
      case typ:Int if(10000 to 21999) contains typ =>
        AlarmDB.getLastAlarmDataByType(device_type:Int,device_id:String,alarm_stop,typ)
    }
  }

}
