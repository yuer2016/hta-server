package com.yicheng.statistics.repo.model

import java.util.Date

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonSubTypes, JsonTypeInfo}
import com.yicheng.statistics.repo.model.Annotation._
import com.yicheng.statistics.repo.model.Constants.Data._

import scala.collection._

/**
  * 终端设备实时上报数据消息类。
  */
object Data {
  //********************************************************************************
  // 终端设备上报数据的实体类定义。
  //********************************************************************************
  trait BaseDataModel extends PublishModel{
    val data_time: Date
  }
  /**
    * 终端设备实时上报数据类的基类。
    *
    * @param device_type (必填)设备类型。
    * @param device_id   (必填)设备ID。格式由对应类型设备自行定义, 设备类型和设备ID组成设备的全局唯一标识。
    * @param data_type   (必填)数据类型。数据类型决定对应派生类的类型。
    * @param data_time   (必填)数据采集的时间。UTC毫秒时间戳。
    */
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "data_type", visible = true)
  @JsonSubTypes(value = Array(
    new JsonSubTypes.Type(value = classOf[DataMqtt], name = "0"),
    new JsonSubTypes.Type(value = classOf[DataVehicle], name = "1"),
    new JsonSubTypes.Type(value = classOf[DataCharger], name = "2"),
    new JsonSubTypes.Type(value = classOf[DataTrack], name = "100"),
    new JsonSubTypes.Type(value = classOf[DataUpload], name = "101")))
  abstract class BaseData(device_type: Int, device_id: String, val data_type: Int, data_time: Date) extends BaseDataModel

  /**
    * MQTT设备上传的实时上报数据类。
    *
    * @param device_type (必填)设备类型。设备类型决定对应的设备信息字段的内容
    * @param device_id   (必填)设备ID。格式由对应类型设备自行定义, 设备类型和设备ID组成设备的全局唯一标识
    * @param data_time   (必填)数据采集的时间。UTC毫秒时间戳。
    * @param track_id    (必填)行程ID。采用行程开始的UTC毫秒时间戳作为行程的ID。
    * @param pos_data    (可选)位置信息。
    * @param obd_data    (可选)OBD信息。
    * @param track_data  (可选)行程统计信息。
    */
  case class DataMqtt(device_type: Int,
                      device_id: String,
                      data_time: Date,
                      track_id: Long,
                      pos_data: Option[DataInfoPos] = None,
                      @DeserInnerIntFloat obd_data: Option[DataInfoObd] = None,
                      @DeserInnerIntInt track_data: Option[DataInfoTrack] = None)
    extends BaseData(device_type, device_id, DATA_MQTT_DEVICE, data_time) with CodeMapModel

  /**
    * 新能源车TBOX设备的实时上报数据类。
    *
    * @param device_type       (必填)设备类型。设备类型决定对应的设备信息字段的内容
    * @param device_id         (必填)设备ID。格式由对应类型设备自行定义, 设备类型和设备ID组成设备的全局唯一标识
    * @param data_time         (必填)数据采集的时间。UTC毫秒时间戳。
    * @param pos_data          (可选)位置数据。对应电动车国标'0x07车辆位置数据'。
    * @param vehicle_data      (可选)整车数据。对应电动车国标'0x03整车数据'。
    * @param vehicle_status    (可选)整车状态数据。
    * @param battery_vol_data  (可选)动力蓄电池电气数据。对应电动车国标'0x01动力蓄电池电气数据'。
    * @param battery_temp_data (可选)动力蓄电池包温度数据。对应电动车国标'0x02动力蓄电池包温度数据'。
    * @param fuel_data         (可选)燃料电池数据。对应电动车国标'0x05燃料电池数据'。
    * @param electric_data     (可选)汽车电机数据。对应电动车国标'0x04汽车电机部分数据'。
    * @param motor_data        (可选)汽车发动机数据。对应电动车国标'0x06汽车发动机部分数据'。
    * @param extreme_data      (可选)极值数据。对应电动车国标'0x08极值数据'。
    * @param dcdc_data         (可选)DCDC数据。
    * @param charge_data       (可选)充电阶段实时状态数据。对应充电机国标'充电阶段数据'。
    */
  case class DataVehicle(device_type: Int,
                         device_id: String,
                         data_time: Date,
                         pos_data: Option[DataInfoPos] = None,
                         vehicle_data: Option[DataInfoVehicle] = None,
                         @DeserInnerIntInt vehicle_status: Option[DataInfoVehicleStatus] = None,
                         battery_vol_data: Option[DataInfoBatteryVolList] = None,
                         battery_temp_data: Option[DataInfoBatteryTempList] = None,
                         fuel_data: Option[DataInfoFuelBattery] = None,
                         electric_data: Option[DataInfoElectricMotorList] = None,
                         motor_data: Option[DataInfoCarMotor] = None,
                         extreme_data: Option[DataInfoExtreme] = None,
                         dcdc_data: Option[DataInfoDcDc] = None,
                         charge_data: Option[DataInfoChargeStage] = None)
    extends BaseData(device_type, device_id, DATA_TBOX_WTM, data_time) with CodeMapModel

  /**
    * 固定充电桩/移动充电桩TBOX设备的实时上报数据类。
    *
    * @param device_type (必填)设备类型。设备类型决定对应的设备信息字段的内容
    * @param device_id   (必填)设备ID。格式由对应类型设备自行定义, 设备类型和设备ID组成设备的全局唯一标识
    * @param data_time   (必填)数据采集的时间。UTC毫秒时间戳。
    * @param charger_id  (必填)充电桩编号/补电车编号。从沃特玛协议的'0x06车载充电机数据'中获取。
    * @param pile_data   (可选)充电桩/车载充电机数据。
    * @param gun_data    (可选)充电枪实时状态数据。
    * @param charge_data (可选)充电枪充电阶段实时状态数据。对应充电机国标'充电阶段数据'。
    */
  case class DataCharger(device_type: Int,
                         device_id: String,
                         data_time: Date,
                         charger_id: String,
                         pile_data: Option[DataInfoChargePile] = None,
                         gun_data: Option[DataInfoChargeGunList] = None,
                         charge_data: Option[DataInfoChargeStageList] = None)
    extends BaseData(device_type, device_id, DATA_CHARGER_WTM, data_time)

  /**
    * MQTT设备的行程信息类。
    *
    * @param device_type  (必填)设备类型。设备类型决定对应的设备信息字段的内容
    * @param device_id    (必填)设备ID。格式由对应类型设备自行定义, 设备类型和设备ID组成设备的全局唯一标识
    * @param track_id     (必填)行程ID。采用行程开始的UTC毫秒时间戳作为行程的ID。
    * @param data_time    (必填)数据采集的时间。UTC毫秒时间戳。
    * @param start_time   (必填)行程的毫秒时间戳的开始时间。
    * @param stop_time    (可选)行程的毫秒时间戳的结束时间。如果为null，则行程未结束。
    * @param is_power_on  (可选)行程启动时是否加电。
    * @param is_power_off (可选)行程结束时是否断电。
    * @param pos_data     (可选)行程结束时位置信息。
    * @param track_data   (可选)行程结束时行程信息。
    * @param stat_data    (可选)行程结束时统计信息。
    * @param stop_pic     (可选)行程结束时停车图片的URL。null - 行程未结束或图片上传失败。
    */
  @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
  case class DataTrack(device_type: Int,
                       device_id: String,
                       data_time: Date,
                       track_id: Long,
                       start_time: Date,
                       stop_time: Option[Date] = None,
                       @DeserValueBool is_power_on: Option[Boolean] = None,
                       @DeserValueBool is_power_off: Option[Boolean] = None,
                       pos_data: Option[DataInfoPos] = None,
                       @DeserInnerIntInt track_data: Option[DataInfoTrack] = None,
                       @DeserInnerValueInt stat_data: Option[DataInfoStat] = None,
                       stop_pic: Option[String] = None)
    extends BaseData(device_type, device_id, DATA_TRACK_DATA, data_time) with CodeMapModel

  /**
    * 终端设备上传的文件消息类。
    * @param id          (可选)数据库中记录ID。
    * @param service_id  (必填)数据库中业务系统ID。
    * @param device_type (必填)设备类型。
    * @param device_id   (必填)设备ID。格式由对应类型设备自行定义, 设备类型和设备ID组成设备的全局唯一标识
    * @param data_time   (必填)数据采集的时间。UTC毫秒时间戳。
    * @param session_id  (必填)上传的唯一标识。如果上传类型为行程结束图片，则必须与行程的track_id相同。
    * @param upload_type (必填)上传数据类型。
    * @param file_url    (必填)上传文件的下载地址。
    * @param file_name   (可选)上传名称。
    * @param file_size   (必填)文件长度。
    * @param mime_type   (必填)文件类型.
    * @param device_data (可选)文件上传时设备的实时数据信息。根据设备类型决定其数据类型。如果是MQTT设备，则为DataMqtt；如果为TBOX设备，则为DataVehicle；如果是充电设备，则为DataCharger。
    */
  @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
  @JsonIgnoreProperties(value = Array("id", "service_id", "file_name"))
  case class DataUpload(id: Option[Long] = None,
                        service_id: Option[String] = None,
                        device_type: Int,
                        device_id: String,
                        data_time: Date,
                        session_id: Long,
                        upload_type: Int,
                        file_url: String,
                        file_name: Option[String] = None,
                        file_size: Long,
                        mime_type: Option[String] = None,
                        device_data: Option[BaseData] = None)
    extends BaseData(device_type, device_id, DATA_UPLOAD_DATA, data_time)

  //********************************************************************************
  // 终端设备上报数据的详情类定义。
  //********************************************************************************
  /**
    * 实时数据详情类的接口类。
    */
  trait DataModel extends BaseModel

  /**
    * 终端设备实时数据中的位置信息。支持GPS定位/基站定位/WIFI定位方式。
    *
    * @param pos_type         (必填)定位类型。0：未定位；1：GPS二维定位；2：GPS三维定位；3：LBS基站定位；4：WIFI定位。
    * @param satellite_cnt    (定位类型位GPS时必填)可见卫星总数。0-12，越多越好。
    * @param pdop             (定位类型位GPS时必填)PDOP位置精度因子。0.5-99.9，越小精度越高。
    * @param latitude         (定位类型位GPS时必填)纬度数据。ddd.ddddd格式，负数：南纬，正数：北纬。
    * @param longitude        (定位类型位GPS时必填)经度数据。ddd.ddddd格式，负数：东经，正数：西经。
    * @param height           (定位类型位GPS时必填)海拔高度。单位：米。
    * @param speed            (定位类型位GPS时必填)地面速率。单位：米/秒。
    * @param real_direction   (定位类型位GPS时必填)真北地面航向角度。单位：度，0-360°。
    * @param magnet_direction (定位类型位GPS时可选)磁北地面航向角度。单位：度，0-360°。
    * @param lbs_cell_array   (定位类型位LBS时必填)LBS小区基站Id和信号强度。LBS小区基站Id的格式为：MCC(3位)+MNC(2位)+LAC(5位)+CID(5位)的15位数字
    * @param wifi_mac_array   (定位类型位WIFI时必填)WIFI热点的MAC地址。格式为：将MAC地址转换成8字节长整数，例如：12:34:56:78:90:AB为0x1234567890ab
    */
  case class DataInfoPos(pos_type: Int,
                         @DeserValueInt satellite_cnt: Option[Int] = None,
                         @DeserValueFloat   pdop: Option[Float] = None,
                         @DeserValueDouble  latitude: Option[Double] = None,
                         @DeserValueDouble  longitude: Option[Double] = None,
                         @DeserValueInt height: Option[Int] = None,
                         @DeserValueInt speed: Option[Int] = None,
                         @DeserValueInt real_direction: Option[Int] = None,
                         @DeserValueInt magnet_direction: Option[Int] = None,
                         @DeserMapLongInt lbs_cell_array: Map[Long, Int] = Map.empty,
                         @DeserValueLong wifi_mac_array: Seq[Long] = Nil) extends DataModel

  /**
    * 终端设备实时数据中的OBD信息。具体参数请参考DataInfoObd对象的常量定义。
    */
  type DataInfoObd = Map[Int, Float]
  def DataInfoObd(xs: (Int, Float)*): DataInfoObd = Map(xs: _*)

  /**
    * MQTT设备实时数据中的行程信息。具体参数请参考DataInfoTrack对象的常量定义。
    */
  type DataInfoTrack = Map[Int, Int]
  def DataInfoTrack(xs: (Int, Int)*): DataInfoTrack = Map(xs: _*)

  /**
    * MQTT设备实时数据中的终端统计信息。具体参数请参考DataInfoStat对象的常量定义。
    */
  type DataInfoStat = Map[String, Int]
  def DataInfoStat(xs: (String, Int)*): DataInfoStat = Map(xs: _*)

  /**
    * 整车数据的结构定义。从沃特玛协议数据汇报消息的'0x02电池包信息'/'0x03整车仪表信息'/'0x05DCDC(24V)信息'中获取相关数据。
    *
    * @param power_status      (必填，国标字段)车辆状态。0x01：启动/keyon，0x02：熄火/keyoff，null：无效。从沃特玛协议的'0x03整车仪表信息'中获取。
    * @param run_status        (必填，国标字段)运行状态。0x01：充电；0x02：行驶；0x03：停止，null：无效。从沃特玛协议的'0x03整车仪表信息'中获取。
    * @param fuel_status       (必填，国标字段)动力运行模式。0x01: 纯电；0x02：混动；0x03：燃油，null：无效。
    * @param current_speed     (必填，国标字段)当前里程表车速。单位：km/h，范围：0.0km/h～220.0km/h，null：无效。从沃特玛协议的'0x03整车仪表信息'中获取。
    * @param current_mileage   (必填，国标字段)当前里程表读数。单位：km，范围：0.0km～999999.9km，null：无效。从沃特玛协议的'0x03整车仪表信息'中获取。
    * @param total_voltage     (必填，国标字段)总电压。单位：V，范围：0.0V～1000.0V，null：无效。从沃特玛协议的'0x02电池包信息'中获取。
    * @param total_current     (必填，国标字段)总电流。单位：A，范围：-1000.0V～+1000.0V，null：无效。从沃特玛协议的'0x02电池包信息'中获取。
    * @param soc_status        (必填，国标字段)SOC状态。范围：0～100，表示0%～100%，null：无效。从沃特玛协议的'0x02电池包信息'中获取。
    * @param dc_status         (必填，国标字段)DC-DC状态。0x01：工作；0x02：断开，null：无效。从沃特玛协议的'0x05DCDC(24V)信息'中获取。
    * @param gear_status       (必填，国标字段)档位状态。0：空挡；1～8：1档～8档，13：倒车挡，14：自动D档，15：停车P档，null：无效。从沃特玛协议的'0x03整车仪表信息'中获取。
    * @param power_value       (必填，国标字段)牵引踏板信号。范围：0～100，表示0%～100%。从沃特玛协议的'0x03整车仪表信息'中获取。
    * @param brake_value       (必填，国标字段)制动踏板信号。范围：0～100，表示0%～100%。从沃特玛协议的'0x03整车仪表信息'中获取。
    * @param resistance        (必填，国标字段)绝缘电阻值。单位：kΩ，范围：0kΩ～60000kΩ，null：无效。从沃特玛协议的'0x02电池包信息'中获取。
    * @param bms_protocol_ver  (可选，扩展字段)BMS协议版本号。从沃特玛协议的'0x02电池包信息'中获取。
    * @param bms_hardware_ver  (可选，扩展字段)BMS硬件版本号。从沃特玛协议的'0x02电池包信息'中获取。
    * @param bms_software_ver  (可选，扩展字段)BMS软件版本号。从沃特玛协议的'0x02电池包信息'中获取。
    * @param bms_extend_ver    (可选，扩展字段)BMS扩展版本号。从沃特玛协议的'0x02电池包信息'中获取。
    * @param remain_power      (可选，扩展字段)剩余能量。单位：KW•H，null：无效。从沃特玛协议的'0x02电池包信息'中获取。
    * @param remain_oil        (可选，扩展字段)剩余油量。单位：L，null：无效。国标808协议的'位置信息汇报'中获取。
    * @param battery_voltage   (可选，扩展字段)车辆蓄电池电压。单位：V，null：无效。从沃特玛协议的'0x03整车仪表信息'中获取。
    */
  case class DataInfoVehicle(@DeserValueInt power_status: Option[Int] = None,
                             @DeserValueInt run_status: Option[Int] = None,
                             @DeserValueInt fuel_status: Option[Int] = None,
                             @DeserValueFloat current_speed: Option[Float] = None,
                             @DeserValueFloat current_mileage: Option[Float] = None,
                             @DeserValueFloat total_voltage: Option[Float] = None,
                             @DeserValueFloat total_current: Option[Float] = None,
                             @DeserValueInt soc_status: Option[Int] = None,
                             @DeserValueInt dc_status: Option[Int] = None,
                             @DeserValueInt gear_status: Option[Int] = None,
                             @DeserValueInt power_value: Option[Int] = None,
                             @DeserValueInt brake_value: Option[Int] = None,
                             @DeserValueInt resistance: Option[Int] = None,
                             bms_protocol_ver: Option[String] = None,
                             bms_hardware_ver: Option[String] = None,
                             bms_software_ver: Option[String] = None,
                             bms_extend_ver: Option[String] = None,
                             @DeserValueFloat remain_power: Option[Float] = None,
                             @DeserValueFloat remain_oil: Option[Float] = None,
                             @DeserValueFloat battery_voltage: Option[Float] = None) extends DataModel

  /**
    * 整车状态数据的结构定义。从沃特玛协议数据汇报消息的'0x02电池包信息'/'0x03整车仪表信息'/'0x05DCDC(24V)信息'中获取相关数据。
    */
  type DataInfoVehicleStatus = Map[Int, Int]
  def DataInfoVehicleStatus(xs: (Int, Int)*): DataInfoVehicleStatus = Map(xs: _*)

  /**
    * 动力蓄电池电气数据的结构定义。从沃特玛协议数据汇报消息的'0x02电池包信息'/'0x09电池包单体电压温度信息'中获取相关数据。
    *
    * @param index               (必填，国标字段)电池总成号
    * @param total_voltage       (必填，国标字段)总电压。单位：V，范围：0.0V～6000.0V，null：无效。从沃特玛协议的'0x02电池包信息'中获取
    * @param total_current       (必填，国标字段)总电流。单位：A，范围：-1000.0A～+1000.0A，null：无效。从沃特玛协议的'0x02电池包信息'中获取
    * @param single_voltage_list (必填，国标字段)单体蓄电池电压值。单位：V，范围：0.0V～15.0V。从沃特玛协议的'0x09电池包单体电压温度信息'中获取
    */
  case class DataInfoBatteryVol(index: Int,
                                @DeserValueFloat total_voltage: Option[Float] = None,
                                @DeserValueFloat total_current: Option[Float] = None,
                                @DeserValueFloat single_voltage_list: Seq[Float]) extends DataModel

  type DataInfoBatteryVolList = Seq[DataInfoBatteryVol]
  def DataInfoBatteryVolList(xs: DataInfoBatteryVol*): DataInfoBatteryVolList = Seq(xs: _*)

  /**
    * 动力蓄电池包温度数据的结构定义。从沃特玛协议数据汇报消息的'0x09电池包单体电压温度信息'中获取相关数据。
    *
    * @param index            (必填，国标字段)电池总成号
    * @param single_temp_list (必填，国标字段)单体电池温度探针的温度值。单位：℃，范围：-40℃～+210℃。从沃特玛协议的'0x09电池包单体电压温度信息'中获取
    */
  case class DataInfoBatteryTemp(index: Int,
                                 @DeserValueInt single_temp_list: Seq[Int]) extends DataModel

  type DataInfoBatteryTempList = Seq[DataInfoBatteryTemp]
  def DataInfoBatteryTempList(xs: DataInfoBatteryTemp*): DataInfoBatteryTempList = Seq(xs: _*)

  /**
    * 燃料电池数据的结构定义。
    *
    * @param total_voltage    (必填，国标字段)总电压。单位：V，范围：0.0V～6000.0V，null：表示无效
    * @param total_current    (必填，国标字段)总电流。单位：A，范围：-1000.0A～+1000.0A，null：表示无效。
    * @param use_rate         (必填，国标字段)燃料消耗率。单位：L/100km，范围：0.0L/100km～600L/100km，null：表示无效。
    * @param single_temp_list (必填，国标字段)燃料电池温度探针的温度值。单位：℃，范围：-30000℃～+30000℃。
    */
  case class DataInfoFuelBattery(@DeserValueFloat total_voltage: Option[Float] = None,
                                 @DeserValueFloat total_current: Option[Float] = None,
                                 @DeserValueFloat use_rate: Option[Float] = None,
                                 @DeserValueInt single_temp_list: Seq[Int]) extends DataModel

  /**
    * 汽车电机数据的结构定义。从沃特玛协议数据汇报消息的'0x04电机信息'中获取相关数据。
    *
    * @param index            (必填，国标字段)电机序号
    * @param motor_status     (必填，国标字段)电机状态。0x01：耗电状态，0x02：发电状态，0x03：关闭状态，null：无效。从沃特玛协议的'0x04电机信息'中获取
    * @param controller_temp  (必填，国标字段)电机控制器温度。单位：℃，范围：-40℃～+210℃，null：无效。从沃特玛协议的'0x04电机信息'中获取
    * @param motor_temp       (必填，国标字段)电机温度。单位：℃，范围：-40℃～+210℃，null：无效。从沃特玛协议的'0x04电机信息'中获取
    * @param motor_speed      (必填，国标字段)实际电机转速。单位：rpm，范围：-2000rpm～45531rpm，null：无效。从沃特玛协议的'0x04电机信息'中获取
    * @param motor_torque     (必填，国标字段)实际电机转矩。单位：N*m，范围：-2000.0N*m～4553.1N*m，null：无效。从沃特玛协议的'0x04电机信息'中获取
    * @param bus_voltage      (必填，国标字段)电机母线电压。单位：V，范围：0V～6000V，null：无效。从沃特玛协议的'0x04电机信息'中获取
    * @param bus_current      (必填，国标字段)电机母线电流。单位：A，范围：-1000.0A～+1000.0A，null：无效。从沃特玛协议的'0x04电机信息'中获取
    * @param contactor_status (可选，扩展字段)电机控制器接触器状态。0x00：未知，0x01：正常，0x02：不正常，null：无效。从沃特玛协议的'0x04电机信息'中获取
    * @param operation_cmd    (可选，扩展字段)电机工作模式指令。null：无效。从沃特玛协议的'0x04电机信息'中获取
    * @param target_speed     (可选，扩展字段)电机目标输出转速。单位：rpm，范围：-2000rpm～45531rpm，null：无效。从沃特玛协议的'0x04电机信息'中获取
    * @param target_torque    (可选，扩展字段)电机目标输出转矩。单位：N*m，范围：-2000.0N*m～4553.1N*m，null：无效。从沃特玛协议的'0x04电机信息'中获取
    */
  case class DataInfoElectricMotor(index: Int,
                                   @DeserValueInt motor_status: Option[Int] = None,
                                   @DeserValueInt controller_temp: Option[Int] = None,
                                   @DeserValueInt motor_temp: Option[Int] = None,
                                   @DeserValueInt motor_speed: Option[Int] = None,
                                   @DeserValueFloat motor_torque: Option[Float] = None,
                                   @DeserValueFloat bus_voltage: Option[Float] = None,
                                   @DeserValueFloat bus_current: Option[Float] = None,
                                   @DeserValueInt contactor_status: Option[Int] = None,
                                   @DeserValueInt operation_cmd: Option[Int] = None,
                                   @DeserValueInt target_speed: Option[Int] = None,
                                   @DeserValueFloat target_torque: Option[Float] = None) extends DataModel

  type DataInfoElectricMotorList = Seq[DataInfoElectricMotor]
  def DataInfoElectricMotorList(xs: DataInfoElectricMotor*): DataInfoElectricMotorList = Seq(xs: _*)

  /**
    * 汽车发动机数据的结构定义。
    *
    * @param motor_status (必填，国标字段)发动机状态。0x01：启动状态，0x02：关闭状态，None：无效
    * @param motor_speed  (必填，国标字段)发动机曲轴转速。单位：rpm，范围：0rpm～60000rpm，None：无效
    * @param use_rate     (必填，国标字段)燃料消耗率。单位：L/100km，范围：0.0L/100km～600L/100km，None：无效
    */
  case class DataInfoCarMotor(@DeserValueInt motor_status: Option[Int] = None,
                              @DeserValueInt motor_speed: Option[Int] = None,
                              @DeserValueFloat use_rate: Option[Float] = None) extends DataModel

  /**
    * 极值数据的结构定义。从沃特玛协议数据汇报消息的'0x02电池包信息'中获取相关数据。
    *
    * @param max_vol_idx    (必填，国标字段)最高电压电池总成号。范围：1～252，null：表示无效
    * @param max_vol_pos    (必填，国标字段)最高电压电池单体串号。范围：1～65535，null：表示无效。从沃特玛协议的'0x02电池包信息'中获取
    * @param max_vol_value  (必填，国标字段)电池单体电压最高值。单位：V，范围：0.0V～15.0V，null：表示无效。从沃特玛协议的'0x02电池包信息'中获取
    * @param min_vol_idx    (必填，国标字段)最低电压电池总成号。范围：1～252，null：表示无效
    * @param min_vol_pos    (必填，国标字段)最低电压电池单体串号。范围：1～65535，null：表示无效。从沃特玛协议的'0x02电池包信息'中获取
    * @param min_vol_value  (必填，国标字段)电池单体电压最低值。单位：V，范围：0.0V～15.0V，null：表示无效。从沃特玛协议的'0x02电池包信息'中获取
    * @param max_temp_idx   (必填，国标字段)蓄电池中最高温度总成号。范围：1～252，null：表示无效
    * @param max_temp_pos   (必填，国标字段)蓄电池中最高温度探针在总成中序号。范围：1～65535，null：表示无效。从沃特玛协议的'0x02电池包信息'中获取
    * @param max_temp_value (必填，国标字段)蓄电池中最高温度值。单位：℃，范围：-40A～+210A，null：表示无效。从沃特玛协议的'0x02电池包信息'中获取
    * @param min_temp_idx   (必填，国标字段)蓄电池中最低温度总成号。范围：1～252，null：表示无效
    * @param min_temp_pos   (必填，国标字段)蓄电池中最低温度探针在总成中序号。范围：1～65535，null：表示无效。从沃特玛协议的'0x02电池包信息'中获取
    * @param min_temp_value (必填，国标字段)蓄电池中最低温度值。单位：℃，范围：-40A～+210A，null：表示无效。从沃特玛协议的'0x02电池包信息'中获取
    */
  case class DataInfoExtreme(@DeserValueInt max_vol_idx: Option[Int] = None,
                             @DeserValueInt max_vol_pos: Option[Int] = None,
                             @DeserValueFloat max_vol_value: Option[Float] = None,
                             @DeserValueInt min_vol_idx: Option[Int] = None,
                             @DeserValueInt min_vol_pos: Option[Int] = None,
                             @DeserValueFloat min_vol_value: Option[Float] = None,
                             @DeserValueInt max_temp_idx: Option[Int] = None,
                             @DeserValueInt max_temp_pos: Option[Int] = None,
                             @DeserValueInt max_temp_value: Option[Int] = None,
                             @DeserValueInt min_temp_idx: Option[Int] = None,
                             @DeserValueInt min_temp_pos: Option[Int] = None,
                             @DeserValueInt min_temp_value: Option[Int] = None) extends DataModel

  /**
    * DCDC数据的结构定义。从沃特玛协议数据汇报消息的'0x05DCDC(24V)信息'中获取相关数据。
    *
    * @param dc_status         (必填，国标字段)DC-DC状态。0x01：工作；0x02：断开，null：无效。从沃特玛协议的'0x05DCDC(24V)信息'中获取。
    * @param dc_input_voltage  (必填，扩展字段)DC-DC输入电压。单位：V，null：无效。从沃特玛协议的'0x05DCDC(24V)信息'中获取。
    * @param dc_output_voltage (必填，扩展字段)DC-DC输出电压。单位：V，null：无效。从沃特玛协议的'0x05DCDC(24V)信息'中获取。
    * @param dc_output_current (必填，扩展字段)DC-DC输出电流。单位：A，null：无效。从沃特玛协议的'0x05DCDC(24V)信息'中获取。
    * @param dc_cool_temp      (必填，扩展字段)DC-DC散热温度。单位：℃，null：无效。从沃特玛协议的'0x05DCDC(24V)信息'中获取。
    */
  case class DataInfoDcDc(@DeserValueInt dc_status: Option[Int] = None,
                          @DeserValueFloat dc_input_voltage: Option[Float] = None,
                          @DeserValueFloat dc_output_voltage: Option[Float] = None,
                          @DeserValueFloat dc_output_current: Option[Float] = None,
                          @DeserValueInt dc_cool_temp: Option[Int] = None) extends DataModel

  /**
    * 车载充电机数据的结构定义。从沃特玛协议数据汇报消息的'0x06车载充电机数据'中获取相关数据。
    *
    * @param charge_start   (必填)充电开始时间。从沃特玛协议的'0x06车载充电机数据'中获取
    * @param start_voltage  (必填)充电开始电压。单位：V。从沃特玛协议的'0x06车载充电机数据'中获取
    * @param start_soc      (必填)充电开始SOC。范围：0～100，表示0%～100%。从沃特玛协议的'0x06车载充电机数据'中获取
    * @param charge_total   (必填)累计充电时间。单位：min。从沃特玛协议的'0x06车载充电机数据'中获取
    * @param max_voltage    (必填)最高允许充电电压。单位：V。从沃特玛协议的'0x06车载充电机数据'中获取
    * @param max_current    (必填)最高允许充电电流。单位：A。从沃特玛协议的'0x06车载充电机数据'中获取
    * @param bms_status     (必填)BMS充电控制状态。从沃特玛协议的'0x06车载充电机数据'中获取
    * @param output_voltage (必填)充电机输出电压。单位：V。从沃特玛协议的'0x06车载充电机数据'中获取
    * @param output_current (必填)充电机输出电流。单位：A。从沃特玛协议的'0x06车载充电机数据'中获取
    * @param error_status   (必填)充电故障状态标志。0：正常，1：故障。从沃特玛协议的'0x06车载充电机数据'中获取
    */
  case class DataInfoChargePile(charge_start: Date,
                                start_voltage: Float,
                                start_soc: Int,
                                charge_total: Int,
                                max_voltage: Float,
                                max_current: Float,
                                bms_status: Int,
                                output_voltage: Float,
                                output_current: Float,
                                error_status: Int) extends DataModel

  /**
    * 充电枪实时状态数据的结构定义。此信息固定充电桩和移动补电车可共用。从沃特玛协议数据汇报消息的'0x07地面充电机数据'/'0x08充电机实时状态信息'中获取相关数据。
    *
    * @param charger_gun    (必填)充电枪编号。范围：1-254，0xff：无效。从沃特玛协议的'0x08充电机实时状态信息'中获取
    * @param charger_status (必填)充电枪状态。0：脱机(枪末接车)，1：空闲(枪接了车末开充充电)，2：握手，3：配置，4：充电中，5：结束，6：故障。从沃特玛协议的'0x08充电机实时状态信息'中获取
    * @param power_required (必填)需求充电量。单位：kw.h。从沃特玛协议的'0x08充电机实时状态信息'中获取
    * @param power_charged  (必填)实际充电量。单位：kw.h。从沃特玛协议的'0x08充电机实时状态信息'中获取
    * @param power_metered  (必填)电表充电量。单位：kw.h。从沃特玛协议的'0x08充电机实时状态信息'中获取
    * @param charge_start   (必填)充电开始时间。从沃特玛协议的'0x08充电机实时状态信息'中获取
    * @param charge_total   (必填)累计充电时间。单位：min。从沃特玛协议的'0x08充电机实时状态信息'中获取
    * @param charge_remain  (必填)估算剩余充电时间。单位：min。从沃特玛协议的'0x08充电机实时状态信息'中获取
    */
  case class DataInfoChargeGun(charger_gun: Int,
                               charger_status: Int,
                               power_required: Float,
                               power_charged: Float,
                               power_metered: Float,
                               charge_start: Date,
                               charge_total: Int,
                               charge_remain: Int) extends DataModel

  type DataInfoChargeGunList = Seq[DataInfoChargeGun]
  def DataInfoChargeGunList(xs: DataInfoChargeGun*): DataInfoChargeGunList = Seq(xs: _*)

  /**
    * 充电阶段实时状态数据的结构定义。此信息固定充电桩和移动补电车可共用。从沃特玛协议数据汇报消息的'0x0A充电阶段信息'中获取相关数据。
    *
    * @param charger_gun     (必填)充电枪编号。范围：1-254，0xff：无效。从沃特玛协议的'0x0A充电阶段信息'中获取。
    * @param require_voltage (必填)电池充电电压需求。单位：V。从沃特玛协议的'0x0A充电阶段信息'中获取。
    * @param require_current (必填)电池充电电流需求。单位：A。从沃特玛协议的'0x0A充电阶段信息'中获取。
    * @param require_mode    (必填)电池充电模式需求。0：末知，1：快冲，2：慢充。从沃特玛协议的'0x0A充电阶段信息'中获取。
    * @param charge_voltage  (必填)充电电压测量值。单位：V。从沃特玛协议的'0x0A充电阶段信息'中获取。
    * @param charge_current  (必填)充电电流测量值。单位：A。从沃特玛协议的'0x0A充电阶段信息'中获取。
    * @param soc_status      (必填)当前荷电状态SOC。范围：0～100，表示0%～100%。从沃特玛协议的'0x0A充电阶段信息'中获取。
    * @param max_vol_value   (必填)最高单体动力蓄电池电压。单位：V。从沃特玛协议的'0x0A充电阶段信息'中获取。
    * @param max_vol_pos     (必填)最高单体蓄电池电压编号。范围：1～65535。从沃特玛协议的'0x0A充电阶段信息'中获取。
    * @param max_temp_value  (必填)最高动力蓄电池温度。单位：℃。从沃特玛协议的'0x0A充电阶段信息'中获取。
    * @param max_temp_pos    (必填)最高温度检测点编号。范围：1～65535。从沃特玛协议的'0x0A充电阶段信息'中获取。
    * @param min_temp_value  (必填)最低动力蓄电池温度。单位：℃。从沃特玛协议的'0x0A充电阶段信息'中获取。
    * @param min_temp_pos    (必填)最低温度检测点编号。范围：1～65535。从沃特玛协议的'0x0A充电阶段信息'中获取。
    * @param charge_remain   (必填)估算剩余充电时间。单位：min。从沃特玛协议的'0x0A充电阶段信息'中获取。
    * @param output_voltage  (必填)充电桩电压输出值。单位：V。从沃特玛协议的'0x0A充电阶段信息'中获取。
    * @param output_current  (必填)充电桩电流输出值。单位：A。从沃特玛协议的'0x0A充电阶段信息'中获取。
    */
  case class DataInfoChargeStage(charger_gun: Int,
                                 require_voltage: Float,
                                 require_current: Float,
                                 require_mode: Int,
                                 charge_voltage: Float,
                                 charge_current: Float,
                                 soc_status: Int,
                                 max_vol_value: Float,
                                 max_vol_pos: Int,
                                 max_temp_value: Int,
                                 max_temp_pos: Int,
                                 min_temp_value: Int,
                                 min_temp_pos: Int,
                                 charge_remain: Int,
                                 output_voltage: Float,
                                 output_current: Float) extends DataModel

  type DataInfoChargeStageList = Seq[DataInfoChargeStage]
  def DataInfoChargeStageList(xs: DataInfoChargeStage*): DataInfoChargeStageList = Seq(xs: _*)
}
