package com.yicheng.statistics.repo.cassandra

import java.time.{LocalDate, LocalDateTime, LocalTime, ZoneId}
import java.util.Date
import scala.concurrent.Await
import scala.concurrent.duration._
import org.scalatest.{FlatSpec, Matchers}
import com.yicheng.statistics.repo.cassandra.CassandraDB._

/**
  * Created by yuer on 2016/11/25.
  */

class cassandraDBSpec extends FlatSpec with Matchers {

  "Test Cassandra connect " should "" in {
    val rs = session.get().execute("select release_version from system.local")
    assert(rs.wasApplied())
  }

  "Test first Time before last Time" should "" in {
    lazy val timeZone = ZoneId.systemDefault
    val firstTime = Date.from(LocalDateTime.of(LocalDate.now.minusDays(1),LocalTime.MIN).
      atZone(timeZone).toInstant)
    val lastTime = Date.from(LocalDateTime.of(LocalDate.now.minusDays(1),LocalTime.MAX).
      atZone(timeZone).toInstant)
    assert(firstTime before lastTime )
  }

  "Test has data in Cassandra" should "" in {
    lazy val timeZone = ZoneId.systemDefault
    val firstTime = Date.from(LocalDateTime.of(LocalDate.now.minusDays(1),LocalTime.MIN).
      atZone(timeZone).toInstant)
    val lastTime = Date.from(LocalDateTime.of(LocalDate.now.minusDays(1),LocalTime.MAX).
      atZone(timeZone).toInstant)
    val result = Await.result(DataMqttDB.list(1,"180922433291231",firstTime,lastTime),120 seconds).filter(
      p =>p.track_data.isDefined && p.track_data.get.nonEmpty && p.track_data.get.get(33554443).isDefined  && p.pos_data.isDefined  &&
        p.pos_data.get.latitude.isDefined && p.pos_data.get.longitude.isDefined)
    println(result.head +":"+ result.last)
    assert(result.nonEmpty)
  }

}
