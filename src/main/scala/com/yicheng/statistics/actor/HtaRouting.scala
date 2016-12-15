package com.yicheng.statistics.actor

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, Props}
import com.yicheng.statistics.repo.RTAModel.BaseAlarm
import com.yicheng.statistics.service.HTAService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Created by yuer on 2016/12/1.
  * hta 路由类
  */

case class BaseAlarmData(alarmType: Int, baseAlarm: BaseAlarm)

class HtaRouting extends Actor with ActorLogging {
  val batteryAlarmActor = context.child("htaAnalysis").
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

    case Stop =>
      context.system.terminate
  }

}

