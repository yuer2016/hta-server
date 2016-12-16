package com.yicheng.statistics.actor

import java.util.Date

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.yicheng.statistics.repo.RTAModel.BaseAlarm
import com.yicheng.statistics.repo.cassandra.DataVehicleDB
import com.yicheng.statistics.service.HTAService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Created by yuer on 2016/12/1.
  * hta 路由类
  */

case class BaseAlarmData(alarmType: Int, baseAlarm: BaseAlarm)

class HtaRouting extends Actor with ActorLogging {
  val batteryAlarmActor:ActorRef = context.child("htaAnalysis").
    getOrElse(context.actorOf(Props[HtaAnalysis], "htaAnalysis"))

  override def receive: Receive = {
    case alarmType: Int =>
      HTAService.getFirstAlarmData(alarmType) onComplete {
        case Success(alarmData) =>
          alarmData foreach { data =>
            log.info("===发送消息{}===",data)
            batteryAlarmActor ! BaseAlarmData(alarmType, data)
          }
        case Failure(error) =>
          log.info(error.getMessage)
      }
    case (deviceId:String,deviceType:Int) =>
      DataVehicleDB.list(deviceType,deviceId,new Date,new Date) onComplete{
        case Success(result) =>
          val filter = result.filter( p => p.vehicle_data.isDefined && p.pos_data.isDefined &&
            p.vehicle_data.get.current_mileage.isDefined && p.pos_data.get.longitude.isDefined &&
            p.pos_data.get.latitude.isDefined)
          batteryAlarmActor ! (filter.head,filter.last)
        case Failure(error) =>
          log.info(error.getMessage)
      }

    case Stop =>
      context.system.terminate
  }

}

