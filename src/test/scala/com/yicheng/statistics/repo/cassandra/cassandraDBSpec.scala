package com.yicheng.statistics.repo.cassandra

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
}
