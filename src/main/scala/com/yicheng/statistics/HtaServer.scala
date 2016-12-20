package com.yicheng.statistics

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import com.yicheng.statistics.actor.HtaRouting
import com.yicheng.statistics.common.AlarmDB
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions._

/**
  * Created by yuer on 2016/11/28.
  */
object HtaServer {
  def main(args: Array[String]) {
    val config = ConfigFactory.load()
    val actorSystem = ActorSystem("HtaServer")
    val routing = actorSystem.actorOf(Props[HtaRouting],"routing")
    config.getIntList("alarm.type") foreach (alarmType =>
      routing ! alarmType
    )
    AlarmDB.getDeviceIdAndType foreach { device =>
      routing ! device
    }

  }
}
