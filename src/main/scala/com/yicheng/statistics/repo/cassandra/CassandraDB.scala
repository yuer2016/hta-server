package com.yicheng.statistics.repo.cassandra

import com.datastax.driver.core._
import com.datastax.driver.core.policies.{RoundRobinPolicy, TokenAwarePolicy}
import com.typesafe.config.{Config, ConfigFactory}
import com.yicheng.statistics._

import scala.collection.JavaConversions._
/**
  * Created by yuer on 2016/11/24.
  */
object CassandraDB {
  val config:Config = ConfigFactory.load()
  val hosts: Seq[String] = config.getStringList("cassandra.db.hosts")
  val port:Int = config.getOptionInt("cassandra.db.port").getOrElse(9042)
  val user:String = config.getOptionString("cassandra.db.user").getOrElse("yczc")
  val password:String = config.getOptionString("cassandra.db.password").getOrElse("admin@yunchuang2016")
  val keyspace:String = config.getOptionString("cassandra.db.keyspace").getOrElse("yczc")

  val poolingOptions: PoolingOptions = new PoolingOptions()
  poolingOptions
    .setCoreConnectionsPerHost(HostDistance.LOCAL, 8)
    .setMaxConnectionsPerHost(HostDistance.LOCAL, 8)
    .setMaxRequestsPerConnection(HostDistance.LOCAL, 2048)
    .setHeartbeatIntervalSeconds(0)
  val socketOptions = new SocketOptions()
  socketOptions.setKeepAlive(true).setTcpNoDelay(true)

  val cluster = Cluster.builder()
    .addContactPoints(hosts: _*)
    .withPort(port)
    .withCredentials(user, password)
    .withProtocolVersion(ProtocolVersion.V4)
    .withPoolingOptions(poolingOptions)
    .withSocketOptions(socketOptions)
    .withLoadBalancingPolicy(new TokenAwarePolicy(new RoundRobinPolicy))
    .build()
  val session = cluster.connectAsync()

  def close() = if (cluster != null) cluster.close()
}
