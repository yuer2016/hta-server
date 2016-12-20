package com.yicheng.statistics.repo.cassandra

import java.time.{LocalDate, LocalDateTime, LocalTime, ZoneId}
import java.util.Date

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

}
