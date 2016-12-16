package com.yicheng.statistics.repo.model

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonInclude}
import com.yicheng.statistics.common.CommonUtils.{PrettyPrint, ReallyEquals}

import scala.collection.{Map, mutable}

/**
  * Created by yuer on 2016/12/16.
  */
/**
  * 实例类接口。统一定义JSON序列化的通用属性。
  */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
trait BaseModel extends Serializable with PrettyPrint with ReallyEquals

/**
  * 所有可能推送给业务系统的实例类接口。
  */
trait PublishModel extends BaseModel {
  val device_type: Int
  val device_id: String
}

/**
  * 属性带Map[Int, T]的Case Class的接口类。toString时输出将Int转换成对应字符串。
  */
trait CodeMapModel extends Product {
  private[this] def toString(x: Any): Any = x match {
    case v: Map[_, _] => if (v.headOption.exists(_._1.isInstanceOf[Int])) CodeMapModel._toString(v.asInstanceOf[Map[Int, _]]) else v.toString
    case Some(v) => toString(v)
    case v => v
  }
  override def toString: String = productIterator.map(toString(_)).mkString(this.productPrefix + "(", ",", ")")
}

// 统一维护和管理数据项编码和名称的映射表。
object CodeMapModel {
  private final lazy val _codeNameMap = mutable.Map[Int, String]()
  def _toString[T](infoMap: Map[Int, T]): String = infoMap.map(it => _name(it._1).map('"' + _ + '"').getOrElse(it._1) + "->" + it._2).mkString("CodeMap(", ",", ")")
  def _register(code_list: (Int, String)*): Unit = _codeNameMap ++= code_list
  def _unregister(code_list: Int*): Unit = _codeNameMap --= code_list
  def _name(code: Int): Option[String] = _codeNameMap.get(code)
  def _code(clazz: Class[_], name: String): Option[Int] = _codeNameMap.find(tuple => tuple._2.equals(name)).map(_._1)
}

/**
  * 常量定义。
  */
object Constants {

  //********************************************************************************
  // 终端类型的常量定义。
  //********************************************************************************
  object Device {
    /** 基于MQTT的后视镜和行车记录仪。使用IMEI为设备ID。 */
    val DEVICE_MQTT_IMEI: Int = 1
    /** 基于MQTT的后视镜和行车记录仪。使用MAC地址为设备ID。 */
    val DEVICE_MQTT_MAC: Int = 2
    /** 沃特玛TBOX的纯电动货车。 */
    val DEVICE_WTM_ELECTRIC_CAR: Int = 11
    /** 沃特玛TBOX的混合动力货车。 */
    val DEVICE_WTM_HYBRID_CAR: Int = 12
    /** 沃特玛TBOX的燃料电池货车。 */
    val DEVICE_WTM_FUEL_CAR: Int = 13
    /** 沃特玛TBOX的固定充电桩。 */
    val DEVICE_WTM_CHARGE_PILE: Int = 14
    /** 沃特玛TBOX的移动充电桩。 */
    val DEVICE_WTM_CHARGE_CAR: Int = 15
    /** 沃特玛TBOX v3.0的纯电动货车。 */
    val DEVICE_WTM_V3_ELECTRIC_CAR: Int = 21
    /** 沃特玛TBOX v3.0的混合动力货车。 */
    val DEVICE_WTM_V3_HYBRID_CAR: Int = 22
    /** 沃特玛TBOX v3.0的燃料电池货车。 */
    val DEVICE_WTM_V3_FUEL_CAR: Int = 23

    /** 设备ID的最小值。格式为: 15位IMEI号或MAC地址的长整数。 */
    val DEVICE_ID_MIN: String = "000000000000000"
    /** 设备ID的最大值。格式为: 15位IMEI号或MAC地址的长整数。 */
    val DEVICE_ID_MAX: String = "999999999999999"
  }

  //********************************************************************************
  // 事件类型的常量定义。
  //********************************************************************************
  object Event {
    /** 设备注册/注销事件。 */
    val EVENT_NOTIFY_REGISTER: Int = 0
    /** 设备行程启动/停止通知事件。 */
    val EVENT_NOTIFY_TRACK: Int = 1
    /** 设备媒体文件上传通知事件。 */
    val EVENT_NOTIFY_UPLOAD: Int = 2
    /** 设备在线活跃状态通知事件。 */
    val EVENT_NOTIFY_ACTIVITY: Int = 3
    /** 设备配置请求事件。 */
    val EVENT_REQUEST_CONFIG: Int = 10
    /** 设备绑定/解绑请求事件。 */
    val EVENT_REQUEST_BIND: Int = 11
    /** 设备流量查询请求事件。 */
    val EVENT_REQUEST_SIMINFO: Int = 12
    /** 充电机充电订单通知事件。 */
    val EVENT_CHARGE_CARD: Int = 20
    /** 充电机刷卡支付通知事件。 */
    val EVENT_CHARGE_PAY: Int = 21
    /** 充电机充电握手通知事件。 */
    val EVENT_CHARGE_HANDSHAKE: Int = 30
    /** 充电机充电参数通知事件。 */
    val EVENT_CHARGE_CONFIG: Int = 31
    /** 充电机充电开始通知事件。 */
    val EVENT_CHARGE_START: Int = 32
    /** 充电机充电结束通知事件。 */
    val EVENT_CHARGE_STOP: Int = 33
    /** 充电机充电错误通知事件。 */
    val EVENT_CHARGE_ERROR: Int = 34
  }

  object EventRequestBind {
    /** 请求绑定。 */
    val BIND_REQUEST: Int = 0
    /** 请求手动解绑。 */
    val UNBIND_USER: Int = 1
    /** 请求设备重置解绑。 */
    val UNBIND_RESET: Int = 2
  }

  object EventChargeCard {
    /** 充电结束方式为充满为止。 */
    val CHARGE_TO_FULL: Int = 0
    /** 充电结束方式为电量控制。 */
    val CHARGE_TO_POWER: Int = 1
    /** 充电结束方式为时间控制。 */
    val CHARGE_TO_TIME: Int = 2
    /** 充电结束方式为金额控制。 */
    val CHARGE_TO_MONEY: Int = 3
  }

  //********************************************************************************
  // 报警类型的常量定义。
  //********************************************************************************
  object Alarm {
    /** 终端设备报警数据的起始类型。 */
    val ALARM_TERMINAL_BEGIN: Int = 1000
    /** 终端设备报警数据的终止类型。 */
    val ALARM_TERMINAL_END: Int = 2000
    /** 终端设备CAN数据报警数据的起始类型。 */
    val ALARM_VEHICLE_BEGIN: Int = 2000
    /** 终端设备CAN数据报警数据的终止类型。 */
    val ALARM_VEHICLE_END: Int = 3000
    /** 充电设备报警数据的起始类型。 */
    val ALARM_CHARGER_BEGIN: Int = 3000
    /** 充电设备报警数据的终止类型。 */
    val ALARM_CHARGER_END: Int = 4000
    /** 接入平台根据终端上报位置数据计算出报警数据的起始类型。 */
    val ALARM_TERMINAL_RTA_BEGIN: Int = 11000
    /** 接入平台根据终端上报位置数据计算出报警数据的终止类型。 */
    val ALARM_TERMINAL_RTA_END: Int = 12000
    /** 接入平台根据终端上报车辆/电池数据计算出报警数据的起始类型。 */
    val ALARM_VEHICLE_RTA_BEGIN: Int = 12000
    /** 接入平台根据终端上报车辆/电池数据计算出报警数据的终止类型。 */
    val ALARM_VEHICLE_RTA_END: Int = 13000
    /** 接入平台根据充电设备上报充电/电池数据计算出报警数据的起始类型。 */
    val ALARM_CHARGER_RTA_BEGIN: Int = 13000
    /** 接入平台根据充电设备上报充电/电池数据计算出报警数据的终止类型。 */
    val ALARM_CHARGER_RTA_END: Int = 14000

    /** 报警级别为无故障。 */
    val ALARM_LEVEL_NORMAL: Int = 0x00
    /** 报警级别为轻微故障。 */
    val ALARM_LEVEL_LIGHT: Int = 0x01
    /** 报警级别为次级故障。 */
    val ALARM_LEVEL_MIDDLE: Int = 0x02
    /** 报警级别为严重故障。 */
    val ALARM_LEVEL_SERIOUS: Int = 0x03
  }

  //********************************************************************************
  // 请求类型的常量定义。
  //********************************************************************************
  object Request {
    /** 发给MQTT设备的请求类型。 */
    val REQUEST_MQTT_MESSAGE = "MQTT"
    /** 发给沃特玛TBOX车辆或充电设备的请求类型。 */
    val REQUEST_WTM_MESSAGE = "WTM"

    /** 发给MQTT设备的终端配置和服务绑定信息的消息类型。 */
    val REQUEST_MQTT_CONFIG = REQUEST_MQTT_MESSAGE + 4
    /** 发给MQTT设备的服务绑定状态信息的消息类型。 */
    val REQUEST_MQTT_BIND = REQUEST_MQTT_MESSAGE + 5
    /** 发给MQTT设备的群组状态信息的消息类型。 */
    val REQUEST_MQTT_GROUP = REQUEST_MQTT_MESSAGE + 6
    /** 发给MQTT设备的SIM开计费信息的消息类型。 */
    val REQUEST_MQTT_SIMINFO = REQUEST_MQTT_MESSAGE + 7
    /** 发给MQTT设备的请求终端执行指定系统操作的消息类型。 */
    val REQUEST_MQTT_SYSTEM = REQUEST_MQTT_MESSAGE + 8
    /** 发给MQTT设备的请求终端启动导航到指定目标点的消息类型。 */
    val REQUEST_MQTT_NAVIGATE = REQUEST_MQTT_MESSAGE + 9
    /** 发给MQTT设备的请求终端进入或退出跟踪模式的消息类型。 */
    val REQUEST_MQTT_TRACK = REQUEST_MQTT_MESSAGE + 10
    /** 发给MQTT设备的请求终端上传指定类型文件的消息类型。 */
    val REQUEST_MQTT_MONITOR = REQUEST_MQTT_MESSAGE + 11
  }

  //********************************************************************************
  // 请求应答类型的常量定义。
  //********************************************************************************
  object Result {
    /** MQTT设备返回的请求结果类型。 */
    val RESULT_MQTT_COMMAND: String = "MQTT"
    /** MQTT设备返回的监控请求结果类型。 */
    val RESULT_MQTT_MONITOR: String = RESULT_MQTT_COMMAND + 11
    /** 发给沃特玛TBOX车辆或充电设备返回的请求结果类型。 */
    val RESULT_WTM_COMMAND: String = "WTM"

    /** 沃特玛TBOX车辆或充电设备返回的通用请求结果的应答类型。 */
    val WTM_RESULT_GENERAL: Int = 0x00
    /** 沃特玛TBOX车辆或充电设备返回的终端运行参数查询结果的应答类型。 */
    val WTM_RESULT_GET_PARAM: Int = 0xC4
    /** 沃特玛TBOX车辆或充电设备返回的数据透传结果的应答类型。 */
    val WTM_RESULT_RAW_DATA: Int = 0xC6
    /** 沃特玛TBOX车辆或充电设备返回的查询车辆参数远程结果的应答类型。 */
    val WTM_RESULT_REMOTE_QUERY: Int = 0xC8
  }

  object ResultWtmGeneral {
    /** 通用请求结果为命令执行失败。 */
    val GENERAL_RESULT_FAILED: Int = 0x0001
    /** 通用请求结果为重复命令(即有相同的命令尚未返回请求结果)。 */
    val GENERAL_RESULT_PENDING: Int = 0xFFFE
    /** 通用请求结果为命令无效。 */
    val GENERAL_RESULT_INVALID: Int = 0xFFFF
    /** 查询终端运行参数的请求结果为终端不支持某些功能。 */
    val GET_PARAM_INVALID_CMD: Int = 0x0201
    /** 查询终端运行参数的请求结果为终端不支持设置中的某个参数。 */
    val GET_PARAM_INVALID_PARAM: Int = 0x0202
    /** 查询终端运行参数的请求结果为未知原因引起参数设置失败。 */
    val GET_PARAM_UNKNOWN_FAIL: Int = 0x0203
    /** 终端URL的请求结果为终端接收指令成功。 */
    val REMOTE_URL_SUCCESS: Int = 0x0101
    /** 终端URL的请求结果为用户名密码不正确。 */
    val REMOTE_URL_AUTH_FAILED: Int = 0x0102
    /** 终端URL的请求结果为文件地址无法访问。 */
    val REMOTE_URL_UNREACHABLE: Int = 0x0103
    /** 终端URL的请求结果为无法上传文件服务器返回无权限。 */
    val REMOTE_URL_NO_PERMISSION: Int = 0x0104
  }

  //********************************************************************************
  // 数据类型的常量定义。
  //********************************************************************************
  object Data {
    /** 基于MQTT的后视镜和行车记录仪设备的实时上报数据。 */
    val DATA_MQTT_DEVICE: Int = 0
    /** 沃特玛TBOX的车辆设备的实时上报数据。 */
    val DATA_TBOX_WTM: Int = 1
    /** 沃特玛TBOX的充电设备的实时上报数据。 */
    val DATA_CHARGER_WTM: Int = 2
    /** 设备上报的行程数据。 */
    val DATA_TRACK_DATA: Int = 100
    /** 设备上报的媒体上传数据。 */
    val DATA_UPLOAD_DATA: Int = 101
  }

  object DataUpload {
    /** 行程结束图片。 */
    val UPLOAD_TRACK: Int = 0
    /** 媒体分享。 */
    val UPLOAD_SHARE: Int = 1
    /** 监控上传。 */
    val UPLOAD_MONITOR: Int = 2
    /** 报警上传。 */
    val UPLOAD_ALARM: Int = 3
    /** 其他上传。 */
    val UPLOAD_OTHER: Int = 100
  }

  object DataInfoPos {
    /** 定位类型为未定位。 */
    val POS_NONE: Int = 0
    /** 定位类型为GPS二维定位。 */
    val POS_GPS_2D: Int = 1
    /** 定位类型为GPS三维定位。 */
    val POS_GPS_3D: Int = 2
    /** 定位类型为LBS基站定位。 */
    val POS_LBS: Int = 3
    /** 定位类型为WIFI定位， */
    val POS_WIFI: Int = 4
  }

  object DataInfoObd {
    private[this] val DATA_INFO_OBD = 0x01000000
    /** (必填)速度。单位：米/秒。 */
    val OBD_SPEED: Int = DATA_INFO_OBD + 1
    /** (必填)里程。单位：米。 */
    val OBD_MILEAGE: Int = DATA_INFO_OBD + 2
    /** (可选)控制模块电压。单位：V。 */
    val OBD_VOLTAGE: Int = DATA_INFO_OBD + 3
    /** (必填)冷却液温度。单位：摄氏度。 */
    val OBD_TEMPERATURE: Int = DATA_INFO_OBD + 4
    /** (必填)发动机负荷比例。单位：%。 */
    val OBD_ENGINE_OVERLOAD: Int = DATA_INFO_OBD + 10
    /** (必填)发动机转速。单位：RPM。 */
    val OBD_ENGINE_ROTATION: Int = DATA_INFO_OBD + 11
    /** (可选)进气管绝对压力。单位：KPA。 */
    val OBD_AIR_PRESSURE: Int = DATA_INFO_OBD + 12
    /** (可选)燃油压力。单位：KPA。 */
    val OBD_OIL_PRESSURE: Int = DATA_INFO_OBD + 13
    /** (可选)瞬时油耗1。单位：升/小时。 */
    val OBD_INSTANT_OIL1: Int = DATA_INFO_OBD + 14
    /** (可选)瞬时油耗2。单位：升/100公里。 */
    val OBD_INSTANT_OIL2: Int = DATA_INFO_OBD + 15
    /** (可选)进气温度。单位：摄氏度。 */
    val OBD_AIR_TEMP: Int = DATA_INFO_OBD + 16
    /** (可选)空气流量。单位：G/S。 */
    val OBD_AIR_VOLUME: Int = DATA_INFO_OBD + 17
    /** (可选)节气门相对位置。 */
    val OBD_THROTTLE_REL_POS: Int = DATA_INFO_OBD + 18
    /** (可选)节气门绝对位置。 */
    val OBD_THROTTLE_ABS_POS: Int = DATA_INFO_OBD + 19
    /** (可选)点火提前角。单位：度。 */
    val OBD_FIRE_ANGLE: Int = DATA_INFO_OBD + 20
    /** (可选)空燃比系数。 */
    val OBD_OIL_RATIO: Int = DATA_INFO_OBD + 21
    /** (可选)长期燃油修正。 */
    val OBD_OIL_CORRECT: Int = DATA_INFO_OBD + 22

    CodeMapModel._register(OBD_SPEED -> "速度", OBD_MILEAGE -> "里程", OBD_VOLTAGE -> "电压", OBD_TEMPERATURE -> "温度",
      OBD_ENGINE_OVERLOAD -> "发动机负荷", OBD_ENGINE_ROTATION -> "发动机转速", OBD_AIR_PRESSURE -> "进气压力", OBD_OIL_PRESSURE -> "燃油压力",
      OBD_INSTANT_OIL1 -> "瞬时油耗1", OBD_INSTANT_OIL2 -> "瞬时油耗2", OBD_AIR_TEMP -> "进气温度", OBD_AIR_VOLUME -> "空气流量", OBD_THROTTLE_REL_POS -> "节气门相对位置",
      OBD_THROTTLE_ABS_POS -> "节气门绝对位置", OBD_FIRE_ANGLE -> "点火提前角", OBD_OIL_RATIO -> "空燃比系数", OBD_OIL_CORRECT -> "长期燃油修正")  }

  object DataInfoTrack {
    private[this] val DATA_INFO_TRACK = 0x02000000
    /** (必填)急加速次数。 */
    val TRACK_ACCELERATE_CNT: Int = DATA_INFO_TRACK + 1
    /** (必填)急减速次数。 */
    val TRACK_DECELERATE_CNT: Int = DATA_INFO_TRACK + 2
    /** (必填)超速次数。 */
    val TRACK_OVERSPEED_CNT: Int = DATA_INFO_TRACK + 3
    /** (必填)车辆翻滚次数。 */
    val TRACK_TURNOVER_CNT: Int = DATA_INFO_TRACK + 4
    /** (可选)行程总耗油量。单位：升。 */
    val TRACK_TOTAL_OIL: Int = DATA_INFO_TRACK + 10
    /** (可选)行程总行驶里程。单位：米。 */
    val TRACK_TOTAL_MILEAGE: Int = DATA_INFO_TRACK + 11
    /** (可选)最大速度。单位：米/秒。 */
    val TRACK_MAX_SPEED: Int = DATA_INFO_TRACK + 12
    /** (可选)发动机最高转速。单位：RPM。 */
    val TRACK_MAX_ROTATE: Int = DATA_INFO_TRACK + 13
    /** (可选)冷却液最高温度。单位：摄氏度。 */
    val TRACK_MAX_TEMP: Int = DATA_INFO_TRACK + 14
    /** (可选)超速行驶的时间(大于120KM/H)。单位：秒。 */
    val TRACK_OVERSPEED_TIME: Int = DATA_INFO_TRACK + 20
    /** (可选)超速行驶的距离(大于120KM/H)。单位：米。 */
    val TRACK_OVERSPEED_RANGE: Int = DATA_INFO_TRACK + 21
    /** (可选)超速行驶的油耗(大于120KM/H)。单位：升。 */
    val TRACK_OVERSPEED_OIL: Int = DATA_INFO_TRACK + 22
    /** (可选)高速行驶的时间(80KM/H-120KM/H)。单位：秒。 */
    val TRACK_HIGHSPEED_TIME: Int = DATA_INFO_TRACK + 23
    /** (可选)高速行驶的距离(80KM/H-120KM/H)。单位：米。 */
    val TRACK_HIGHSPEED_RANGE: Int = DATA_INFO_TRACK + 24
    /** (可选)高速行驶的油耗(80KM/H-120KM/H)。单位：升。 */
    val TRACK_HIGHSPEED_OIL: Int = DATA_INFO_TRACK + 25
    /** (可选)中速行驶的时间(40KM/H-80KM/H)。单位：秒。 */
    val TRACK_MIDSPEED_TIME: Int = DATA_INFO_TRACK + 26
    /** (可选)中速行驶的距离(40KM/H-80KM/H)。单位：米。 */
    val TRACK_MIDSPEED_RANGE: Int = DATA_INFO_TRACK + 27
    /** (可选)中速行驶的油耗(40KM/H-80KM/H)。单位：升。 */
    val TRACK_MIDSPEED_OIL: Int = DATA_INFO_TRACK + 28
    /** (可选)低速行驶的时间(1KM/H-40KM/H)。单位：秒。 */
    val TRACK_LOWSPEED_TIME: Int = DATA_INFO_TRACK + 29
    /** (可选)低速行驶的距离(1KM/H-40KM/H)。单位：米。 */
    val TRACK_LOWSPEED_RANGE: Int = DATA_INFO_TRACK + 30
    /** (可选)低速行驶的油耗(1KM/H-40KM/H)。单位：升。 */
    val TRACK_LOWSPEED_OIL: Int = DATA_INFO_TRACK + 31
    /** (可选)怠速的时间。单位：秒。 */
    val TRACK_IDLE_TIME: Int = DATA_INFO_TRACK + 32
    /** (可选)怠速的油耗。单位：升。 */
    val TRACK_IDLE_OIL: Int = DATA_INFO_TRACK + 33

    CodeMapModel._register(TRACK_ACCELERATE_CNT -> "急加速次数", TRACK_DECELERATE_CNT -> "急减速次数", TRACK_OVERSPEED_CNT -> "超速次数",
      TRACK_TURNOVER_CNT -> "翻滚次数", TRACK_TOTAL_OIL -> "总耗油量", TRACK_TOTAL_MILEAGE -> "行驶里程", TRACK_MAX_SPEED -> "最大速度", TRACK_MAX_ROTATE -> "最高转速",
      TRACK_MAX_TEMP -> "最高温度", TRACK_OVERSPEED_TIME -> "超速行驶时间", TRACK_OVERSPEED_RANGE -> "超速行驶距离", TRACK_OVERSPEED_OIL -> "超速行驶油耗",
      TRACK_HIGHSPEED_TIME -> "高速行驶时间", TRACK_HIGHSPEED_RANGE -> "高速行驶距离", TRACK_HIGHSPEED_OIL -> "高速行驶油耗", TRACK_MIDSPEED_TIME -> "中速行驶时间",
      TRACK_MIDSPEED_RANGE -> "中速行驶距离", TRACK_MIDSPEED_OIL -> "中速行驶油耗", TRACK_LOWSPEED_TIME -> "低速行驶时间", TRACK_LOWSPEED_RANGE -> "低速行驶距离",
      TRACK_LOWSPEED_OIL -> "低速行驶油耗", TRACK_IDLE_TIME -> "怠速时间", TRACK_IDLE_OIL -> "怠速油耗")
  }

  object DataInfoStat {
    /** 预约导航操作次数统计项 */
    val STAT_NAVIGATE: String = "nav"
    /** 行车记录操作次数统计项 */
    val STAT_RECORD: String = "rec"
    /** 声音调节操作次数统计项 */
    val STAT_VOLUMN: String = "vol"
    /** 导航历史操作次数统计项 */
    val STAT_HISTORY: String = "his"
    /** 系统升级操作次数统计项 */
    val STAT_UPGRADE: String = "up"
    /** WIFI设置操作次数统计项 */
    val STAT_WIFI: String = "wifi"
    /** 亮度调节操作次数统计项 */
    val STAT_BRIGHT: String = "bright"
    /** 安全预警操作次数统计项 */
    val STAT_ALARM: String = "alarm"
    /** 水平校准操作次数统计项 */
    val STAT_LEVEL: String = "level"
    /** 设备信息操作次数统计项 */
    val STAT_INFO: String = "info"
    /** GPS测试操作次数统计项 */
    val STAT_TEST: String = "test"
  }

  object DataInfoVehicle {
    /** 车辆状态为启动(keyon)。 */
    val POWER_STATUS_KEY_ON: Int = 1
    /** 车辆状态为熄火(keyoff)。 */
    val POWER_STATUS_KEY_OFF: Int = 2
    /** 车辆状态为充电状态。 */

    val RUN_STATUS_CHARGING: Int = 1
    /** 车辆状态为行驶状态。 */
    val RUN_STATUS_DRIVING: Int = 2
    /** 车辆状态为停止状态。 */
    val RUN_STATUS_STOPPED: Int = 3

    /** 动力运行模式为充电。 */
    val FUEL_STATUS_ELECTRICITY: Int = 1
    /** 动力运行模式为混动。 */
    val FUEL_STATUS_HYBRID: Int = 2
    /** 动力运行模式为燃油。 */
    val FUEL_STATUS_FUEL: Int = 3

    /** DC-DC状态为工作。 */
    val DCDC_STATUS_WORKING: Int = 1
    /** DC-DC状态为断开。 */
    val DCDC_STATUS_STOPPED: Int = 2

    /** 档位状态为空挡。 */
    val GEAR_STATUS_NEUTRAL: Int = 0
    /** 档位状态为倒车挡。 */
    val GEAR_STATUS_REVERSE: Int = 13
    /** 档位状态为自动D档。 */
    val GEAR_STATUS_D_MODE: Int = 14
    /** 档位状态为停车P档。 */
    val GEAR_STATUS_P_MODE: Int = 15
  }

  object DataInfoElectricMotor {
    /** 电机状态为耗电状态。 */
    val MOTOR_STATUS_CONSUMING: Int = 1
    /** 电机状态为发电状态。 */
    val MOTOR_STATUS_GENERATING: Int = 2
    /** 电机状态为关闭状态。 */
    val MOTOR_STATUS_TURNOFF: Int = 3

    /** 电机控制器接触器状态为未知。 */
    val CONTACTOR_STATUS_UNKNOWN: Int = 0
    /** 电机控制器接触器状态为正常。 */
    val CONTACTOR_STATUS_NORMAL: Int = 1
    /** 电机控制器接触器状态为不正常。 */
    val CONTACTOR_STATUS_ABNORMAL: Int = 2
  }

  object DataInfoCarMotor {
    /** 发动机状态为启动状态。 */
    val ENGINE_STATUS_STARTED: Int = 1
    /** 发动机状态为关闭状态。 */
    val ENGINE_STATUS_STOPPED: Int = 2
  }

  object DataInfoChargeGun {
    /** 充电枪状态为脱机状态(枪末接车)。 */
    val CHARGE_STATUS_OFFLINE: Int = 0
    /** 充电枪状态为空闲状态(枪接了车末开充充电)。 */
    val CHARGE_STATUS_IDLE: Int = 1
    /** 充电枪状态为握手状态。 */
    val CHARGE_STATUS_HANDSHAKE: Int = 2
    /** 充电枪状态为配置状态。 */
    val CHARGE_STATUS_CONFIG: Int = 3
    /** 充电枪状态为充电中状态。 */
    val CHARGE_STATUS_CHARGING: Int = 4
    /** 充电枪状态为结束状态。 */
    val CHARGE_STATUS_FINISHED: Int = 5
    /** 充电枪状态为故障状态。 */
    val CHARGE_STATUS_ERROR: Int = 6
  }

  object DataInfoChargeStage {
    /** 电池充电模式需求为末知。 */
    val CHARGE_MODE_UNKNOWN: Int = 0
    /** 电池充电模式需求为快冲。 */
    val CHARGE_MODE_FAST: Int = 1
    /** 电池充电模式需求为慢充。 */
    val CHARGE_MODE_SLOW: Int = 2
  }

  object DataInfoVehicleStatus {
    private[this] val DATA_INFO_VEHICLE_STATUS = 0x03000000
    /** (可选，扩展字段)继电器状态。BIT0-BIT7分别表示RELAY_1~RELAY_8状态，对应位为1则有效，否则无效。从沃特玛协议的'0X02电池包信息'中获取。 */
    val RELAY_STATUS: Int = DATA_INFO_VEHICLE_STATUS + 1
    /** (可选，扩展字段)均衡激活。0：无均衡，1：均衡激活中，NULL：无效。从沃特玛协议的'0X02电池包信息'中获取。 */
    val BALANCE_STATUS: Int = DATA_INFO_VEHICLE_STATUS + 2
    /** (可选，扩展字段)锁车状态。0：未锁定，1：锁定，NULL：无效。从沃特玛协议的'0X03整车仪表信息'中获取。 */
    val LOCK_STATUS: Int = DATA_INFO_VEHICLE_STATUS + 3
    /** (可选，扩展字段)安全带状态。0：松开，1：扣紧，NULL：无效。从沃特玛协议的'0X03整车仪表信息'中获取。 */
    val BELT_STATUS: Int = DATA_INFO_VEHICLE_STATUS + 4
    /** (可选，扩展字段)手刹状态。0：松开，1：制动，NULL：无效。从沃特玛协议的'0X03整车仪表信息'中获取。 */
    val BRAKE_STATUS: Int = DATA_INFO_VEHICLE_STATUS + 5
    /** (可选，扩展字段)车门状态。0：未锁定，1：锁定，NULL：无效。从沃特玛协议的'0X03整车仪表信息'中获取。 */
    val DOOR_STATUS: Int = DATA_INFO_VEHICLE_STATUS + 6
    /** (可选，扩展字段)车窗状态。0：未锁定，1：锁定，NULL：无效。从沃特玛协议的'0X03整车仪表信息'中获取。 */
    val WINDOW_STATUS: Int = DATA_INFO_VEHICLE_STATUS + 7
    /** (可选，扩展字段)空调状态。0：停止，1：正常工作，2：故障，NULL：无效。国标808协议的'位置信息汇报'中获取。 */
    val CONDITION_STATUS: Int = DATA_INFO_VEHICLE_STATUS + 8
    /** (可选，扩展字段)助力状态。0：停止，1：正常工作，2：故障，NULL：无效。从沃特玛协议的'0X03整车仪表信息'中获取。 */
    val ASSIST_STATUS: Int = DATA_INFO_VEHICLE_STATUS + 9
    /** (可选，扩展字段)电路状态。0：停止，1：正常工作，2：故障，NULL：无效。国标808协议的'位置信息汇报'中获取。 */
    val ELECTRIC_STATUS: Int = DATA_INFO_VEHICLE_STATUS + 10
    /** (可选，扩展字段)油泵状态。0：停止，1：正常工作，2：故障，NULL：无效。从沃特玛协议的'0X03整车仪表信息'中获取。 */
    val OIL_STATUS: Int = DATA_INFO_VEHICLE_STATUS + 11
    /** (可选，扩展字段)水泵状态。0：停止，1：正常工作，2：故障，NULL：无效。从沃特玛协议的'0X03整车仪表信息'中获取。 */
    val WATER_STATUS: Int = DATA_INFO_VEHICLE_STATUS + 12
    /** (可选，扩展字段)风扇状态。0：停止，1：正常工作，2：故障，NULL：无效。从沃特玛协议的'0X03整车仪表信息'中获取。 */
    val FAN_STATUS: Int = DATA_INFO_VEHICLE_STATUS + 13
    /** (可选，扩展字段)运营状态。0：运营状态，1：停运状态，NULL：无效。国标808协议的'位置信息汇报'中获取。 */
    val OPERATE_STATUS: Int = DATA_INFO_VEHICLE_STATUS + 14
    /** (可选，扩展字段)负载状态。0：空车，1：半载，2：保留，3：满载，NULL：无效。国标808协议的'位置信息汇报'中获取。 */
    val LOAD_STATUS: Int = DATA_INFO_VEHICLE_STATUS + 15
    /** (可选，扩展字段)车辆故障状态。0：正常，其他：故障，NULL：无效。从沃特玛协议的'0X03整车仪表信息'中获取。 */
    val ERROR_STATUS: Int = DATA_INFO_VEHICLE_STATUS + 100

    CodeMapModel._register(RELAY_STATUS -> "继电器状态", BALANCE_STATUS -> "均衡激活", LOCK_STATUS -> "锁车状态",
      BELT_STATUS -> "安全带状态", BRAKE_STATUS -> "手刹状态", DOOR_STATUS -> "车门状态", WINDOW_STATUS -> "车窗状态", CONDITION_STATUS -> "空调状态",
      ASSIST_STATUS -> "助力状态", ELECTRIC_STATUS -> "电路状态", OIL_STATUS -> "油泵状态", WATER_STATUS -> "水泵状态",
      FAN_STATUS -> "风扇状态", OPERATE_STATUS -> "运营状态", LOAD_STATUS -> "负载状态", ERROR_STATUS -> "车辆故障状态")
  }
}

