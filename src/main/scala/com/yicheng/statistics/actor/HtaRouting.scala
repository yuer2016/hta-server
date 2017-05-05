package com.yicheng.statistics.actor

import java.time.{LocalDate, LocalDateTime, LocalTime, ZoneId}
import java.util.Date

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.yicheng.statistics.repo.RTAModel.BaseAlarm
import com.yicheng.statistics.repo.cassandra.{DataMqttDB, DataVehicleDB}
import com.yicheng.statistics.service.HTAService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Created by yuer on 2016/12/1.
  * hta 路由类
  */

case class BaseAlarmData(alarmType: Int, baseAlarm: BaseAlarm)

class HtaRouting extends Actor with ActorLogging {
  val batteryAlarmActor: ActorRef = context.child("htaAnalysis").
    getOrElse(context.actorOf(Props[HtaAnalysis], "htaAnalysis"))
  lazy val timeZone: ZoneId = ZoneId.systemDefault

  override def receive: Receive = {
    case alarmType: Int =>
      HTAService.getFirstAlarmData(alarmType) onComplete {
        case Success(alarmData) =>
          alarmData foreach { data =>
            log.info("===发送消息{}===", data)
            batteryAlarmActor ! BaseAlarmData(alarmType, data)
          }
        case Failure(error) =>
          log.info(error.getMessage)
      }
    case (deviceId: String, deviceType: Int) =>
      val firstTime = Date.from(LocalDateTime.of(LocalDate.now.minusDays(1), LocalTime.MIN).
        atZone(timeZone).toInstant)
      val lastTime = Date.from(LocalDateTime.of(LocalDate.now.minusDays(1), LocalTime.MAX).
        atZone(timeZone).toInstant)
      DataMqttDB.list(deviceType, deviceId, firstTime, lastTime) onComplete {
        case Success(result) =>
          val filter = result.filter(p => p.track_data.isDefined && p.track_data.get.nonEmpty &&
            p.track_data.get.get(33554443).isDefined && p.pos_data.isDefined &&
            p.pos_data.get.latitude.isDefined && p.pos_data.get.longitude.isDefined)
          batteryAlarmActor ! (filter.head, filter.last)
        case Failure(error) =>
          log.info(error.getMessage)
      }

    case Stop =>
      context.system.terminate
  }

}

