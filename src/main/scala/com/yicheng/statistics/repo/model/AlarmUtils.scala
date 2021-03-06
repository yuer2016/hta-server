package com.yicheng.statistics.repo.model

/**
  * Created by yuer on 2016/12/1.
  */
object AlarmUtils {

  val battery_alarm = 20

  val vehicle_speed = 11000

  val vehicle_tired = 11001

  val BATTERY_ALARM_BEGIN = 12000

  val BATTERY_ALARM_END = 12999


  lazy val RtaAlarmType2AlarmTypeId = Map(
    "R000001" -> 11000, 	//超速报警
    "R000002" -> 11001, 	//疲劳驾驶
    "R000003" -> 11002, 	//怠速报警
    "R000004" -> 11003,   //离线报警
    "R000005" -> 11004,   //驶入区域报警
    "R000006" -> 11005,   //驶出区域报警
    "R000007" -> 11006,   //时间栅栏报警
    "R000008" -> 11007,   //道路偏移报警
    "R000009" -> 11008,   //怠速报警
    "R000010" -> 11009,   //进入地点报警
    "R000011" -> 11010,   //驶出地点报警

    "R100011" -> 12001,  //	电池电量低报警
    "R100012" -> 12002,  //	单体欠压报警
    "R100013" -> 12003,  //	单体过压报警
    "R100014" -> 12004,  //	电池压差报警
    "R100015" -> 12005,  //	相邻两串压差报警分析器
    "R100016" -> 12006,  // 充高放低报警设置

    "R200016" -> 12007,  //	温度过高报警
    "R200017" -> 12008,  //	温感线异常告警
    "R200018" -> 12009,  //	绝缘过低报警
    "R200019" -> 12010,  //	SOC过低报警设置
    "R200020" -> 12011   //	温差报警条件设置
  )

  def alarmSource(alarmType: Int):Int = alarmType match {
    case tpe if (1000 to 1999) contains tpe => 1           //AlarmTerminal
    case tpe if (2000 to 2999) contains tpe => 2           //AlarmVehicle
    case tpe if (3000 to 3999) contains tpe => 3           //AlarmCharger
    case tpe if (11000 to 11999) contains tpe => 10        //AlarmTerminalRTA
    case tpe if (12000 to 12999) contains tpe => 10        //AlarmVehicleRTA
    case tpe if (13000 to 13999) contains tpe => 10        //AlarmChargerRTA
    case tpe if (20000 to 29999) contains tpe => 20
  }

}
