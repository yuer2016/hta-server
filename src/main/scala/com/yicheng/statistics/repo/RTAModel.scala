package com.yicheng.statistics.repo

import java.util.Date

import com.yicheng.statistics.repo.model.Annotation.{DeserValueDouble, DeserValueInt}

/**
  * Created by yuer on 2016/11/29.
  */
object RTAModel {

  /**
    * 终端设备报警消息类。
    * @param device_type  (必填)设备类型。
    * @param device_id    (必填)设备ID。格式由对应类型设备自行定义, 设备类型和设备ID组成设备的全局唯一标识。
    * @param alarm_type   (必填)报警类型。与报警类型字典表的数据一致，报警类型的范围决定对应的报警派生类的类型。
    * @param alarm_source (必填)报警来源。根据类型ID范围填写。
    * @param alarm_cnt    (必填)持续报警的次数。
    * @param alarm_time   (必填)本次报警的时间。UTC毫秒时间戳。
    * @param alarm_start  (可选)报警的开始时间。UTC毫秒时间戳。
    * @param alarm_stop   (可选)报警的结束时间。UTC毫秒时间戳。如果非null，则表示报警结束事件。
    * @param alarm_level  (可选)报警级别。0：无故障，1：轻微故障，2：次级故障，3：严重故障，其他：自定义，null：未指定。
    * @param alarm_data   (必填)报警相关数据，Json格式。
    * @param latitude     (可选)纬度数据。ddd.ddddd格式，负数：南纬，正数：北纬。
    * @param longitude    (可选)经度数据。ddd.ddddd格式，负数：东经，正数：西经。
    * @param height       (可选)海拔高度。单位：米。
    * @param speed        (可选)地面速率。单位：米/秒。
    * @param direction    (可选)地面航向角度。单位：度，0-360°。
    * @param maxspeed     (可选,扩展字段)最大行驶速度
    * @param minspeed     (可选,扩展字段)最小行驶速度
    * @param averagespeed (可选,扩展字段)平均行驶速度
    */
  case class BaseAlarm(
                        device_type: Int,
                        device_id: String,
                        alarm_type: Int,
                        alarm_source: Int,
                        alarm_cnt: Int,
                        alarm_time: Date,
                        alarm_start: Option[Date] = None,
                        alarm_stop: Option[Date] = None,
                        @DeserValueInt alarm_level: Option[Int] = None,
                        alarm_data: String,//Json格式
                        @DeserValueDouble latitude: Option[Double] = None,
                        @DeserValueDouble longitude: Option[Double] = None,
                        @DeserValueInt height: Option[Int] = None,
                        @DeserValueInt speed: Option[Int] = None,
                        @DeserValueInt direction: Option[Int] = None,
                        maxspeed:Option[Int] = None,
                        minspeed:Option[Int] = None,
                        averagespeed:Option[Double] = None
                      )

  /**
    * 电池报警统计类
    *  @param device_type 设备类型
    *  @param device_id 设备ID
    *  @param alarmstarttime 报警开始时间
    *  @param alarmendtime 报警结束时间
    *  @param startlon 开始经度
    *  @param startlat 开始纬度
    *  @param endlon 结束经度
    *  @param endlat 结束纬度
    *  @param startmileage 开始里程
    *  @param endmileage 结束里程
    *  @param createtime 创建时间
    *  @param alarmtype 报警类型
    *  @param alarmbeginvalue 报警开始值
    *  @param alarmendvalue 报警结束值
    *  @param alarmlevel 报警等级
    *  @param alarmunitname 报警单位
    *  @param analysegroupsid  分析组ID
    *  @param remark 备注
    *
    * */
  case class BatteryAlarm(device_type: Int,
                          device_id: String,
                          alarmstarttime:Option[Date] = None,
                          alarmendtime:Option[Date] = None,
                          startlon:Option[Double] = None,
                          startlat:Option[Double] = None,
                          endlon:Option[Double] = None,
                          endlat:Option[Double] = None,
                          startmileage:Option[Int] = None,
                          endmileage:Option[Int] = None,
                          createtime:Date,
                          alarmtype:Int,
                          alarmbeginvalue:Option[String] = None,
                          alarmendvalue:Option[String] = None,
                          alarmlevel:Option[Int] = None,
                          alarmunitname:Option[String] = None,
                          analysegroupsid:Option[Int] = None,
                          remark:Option[String] = None)

  /**
    * 疲劳驾驶报警统计类
    * @param device_type 设备类型
    * @param device_id 设备ID
    * @param startdatetime 开车时间
    * @param enddatetime 疲劳结束时间
    * @param mileagethreshold 疲劳距离门限
    * @param timethreshold 疲劳时间门限
    * @param totaltime 疲劳时长
    * @param startlon 疲劳开始经度
    * @param startlat 疲劳开始纬度
    * @param endlon 疲劳结束经度
    * @param endlat 疲劳结束纬度
    * @param startmileage 疲劳开始里程
    * @param endmileage 疲劳结束里程
    * @param totalmileage 疲劳总里程
    * @param analysegroupsid 分析组id
    * @param analyseconditions 分析组名称
    * @param createdatetime 创建时间
    * @param starttiredtime 疲劳开始时间
    * */
  case class VehicleTired(device_type: Int,
                          device_id: String,
                          startdatetime:Option[Date]= None,
                          enddatetime:Option[Date]= None,
                          mileagethreshold:Option[Int]= None,
                          timethreshold:Option[Int]= None,
                          totaltime:Option[Int]= None,
                          startlon:Option[Double] = None,
                          startlat:Option[Double] = None,
                          endlon:Option[Double] = None,
                          endlat:Option[Double] = None,
                          startmileage:Option[Int]= None,
                          endmileage:Option[Int]= None,
                          totalmileage:Option[Int]= None,
                          analysegroupsid:Option[Int]= None,
                          analyseconditions:Option[String] = None,
                          createdatetime:Date,
                          starttiredtime:Option[Date]= None)

  /**
    * 车辆超速报警统计类
    * @param device_type 设备类型
    * @param device_id 设备ID
    * @param startdatetime 超速开始时间
    * @param enddatetime 超速结束时间
    * @param totaltime 超速时长
    * @param maxspeed 最高速度
    * @param minspeed 最低速度
    * @param averagespeed 平均速度
    * @param speedthreshold 速度门限值
    * @param speedtype 超速类型
    * @param startlon 开始经度
    * @param startlat 开始纬度
    * @param endlon 结束经度
    * @param endlat 结束纬度
    * @param analysegroupsid 分析组id
    * @param analyseconditions 分析组名称
    * @param createdatetime 创建时间
    * @param lineId 在线id
    * @param totalmileage 总里程数
    * */
  case class VehicleSpeed(device_type: Int,
                          device_id: String,
                          startdatetime:Option[Date]= None,
                          enddatetime:Option[Date]= None,
                          totaltime:Option[Int]= None,
                          maxspeed:Option[Int]= None,
                          minspeed:Option[Int]= None,
                          averagespeed:Option[Double]= None,
                          speedthreshold:Option[Int]= None,
                          speedtype:Option[String] = None,
                          startlon:Option[Double]= None,
                          startlat:Option[Double]= None,
                          endlon:Option[Double]= None,
                          endlat:Option[Double]= None,
                          analysegroupsid:Option[Int]= None,
                          analyseconditions:Option[String] = None,
                          createdatetime:Date,
                          lineId:Option[Int]= None,
                          totalmileage:Option[Int]= None)



  /**
    * 进出区域统计分析
    * @param device_type 设备类型
    * @param device_id 设备ID
    * @param intodatetime 进入时间
    * @param outdatetime 离开时间
    * @param totaltime 区域内时长(秒)
    * @param intolon 进入经度
    * @param intolat 进入纬度
    * @param outlon 离开经度
    * @param outlat 离开纬度
    * @param coverageid 区域id
    * @param coveragename 区域名称
    * @param analysegroupsid 分析组id
    * @param analyseconditions 分析组名称
    * @param createdatetime 创建时间
    * @param intomileage 进入区域里程
    * @param outmileage 离开区域里程
    */
  case class AreaInout(device_type:Int,
                       device_id:String,
                       intodatetime:Date,
                       outdatetime:Date,
                       totaltime:Int,
                       intolon:Option[Double]= None,
                       intolat:Option[Double]= None,
                       outlon:Option[Double]= None,
                       outlat:Option[Double]= None,
                       coverageid:Option[Int]= None,
                       coveragename:Option[String]= None,
                       analysegroupsid:Option[Int]= None,
                       analyseconditions:Option[String]= None,
                       createdatetime:Date,
                       intomileage:Option[Int]= None,
                       outmileage:Option[Int]= None)

  /**
    * 行驶里程统计
    * @param device_type 设备类型
    * @param device_id 设备ID
    * @param startmileage 开始里程
    * @param endmileage 结束里程
    * @param starttime 开始时间
    * @param endtime 结束时间
    * @param analysedate 分析日期
    * @param createtime 创建时间
    * @param remark 备注
    * @param startlon 开始经度
    * @param startlat 开始纬度
    * @param endlon 结束经度
    * @param endlat 结束纬度
    * @param totalshock 总耗电量
    *
    * */
  case class VehicleMileage(device_type:Int,
                            device_id:String,
                            startmileage:Option[Float] =None,
                            endmileage:Option[Float] =None,
                            starttime:Date,
                            endtime:Date,
                            analysedate:Date,
                            createtime:Date,
                            remark:Option[String] = None,
                            startlon:Option[Double]= None,
                            startlat:Option[Double]= None,
                            endlon:Option[Double]= None,
                            endlat:Option[Double]= None,
                            totalshock:Option[String] = None)
  /**
    * 驾驶行为统计
    * @param device_type 设备类型
    * @param device_id 设备ID
    * @param time 发生时间
    * @param longitude 开始精度
    * @param latitude 开始纬度
    * @param totalmileage 总里程
    * @param createdatetime 创建时间
    * @param vdbtype 类型
    * */
  case class VehicleDrivingBehavior(device_type: Int,
                                    device_id: String,
                                    time:Date,
                                    longitude:Double,
                                    latitude:Double,
                                    totalmileage:Option[Int] = None,
                                    createdatetime:Date,
                                    vdbtype:Int)

}
