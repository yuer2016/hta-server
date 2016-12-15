package com.yicheng.statistics.common

import java.util.Date

import slick.jdbc._

/**
  * Created by yuer on 2016/11/24.
  */
trait DBCompoent {
  val driver: JdbcProfile
  val db: driver.api.Database
}

/**
  * RTA数据库连接
  */
trait RTADBCompoent extends DBCompoent{
  val driver = MySQLProfile
  val db = RTADB.connectionPool
  val alarm = RTADB.alarm_connectionPool

  implicit object GetUtilDate extends GetResult[java.util.Date] {
    override def apply(rs: PositionedResult): java.util.Date = {
      new Date(rs.nextTimestamp().getTime)
    }
  }

  implicit object GetUtilDateOption extends GetResult[Option[java.util.Date]] {
    override def apply(rs: PositionedResult): Option[java.util.Date] = {
      rs.nextTimestampOption match {
        case Some(res) =>
          Some(new Date(res.getTime))
        case None => None
      }
    }
  }

  implicit object SetUtilDate extends SetParameter[java.util.Date]{
    override def apply(v1: Date, pp: PositionedParameters){
      pp.setTimestamp(new java.sql.Timestamp(v1.getTime))
    }
  }

  implicit object SetUtilDateOption extends SetParameter[Option[java.util.Date]]{
    override def apply(v1: Option[Date], pp: PositionedParameters) {
      var timestamp = Option.empty[java.sql.Timestamp]
      v1 foreach { date =>
        timestamp = Some(new java.sql.Timestamp(date.getTime))
      }
      pp.setTimestampOption(timestamp)
    }
  }
}

private[common] object RTADB {
  val connectionPool = slick.jdbc.MySQLProfile.api.Database.forConfig("rtadb")
  val alarm_connectionPool = slick.jdbc.MySQLProfile.api.Database.forConfig("alarmdb")
}


