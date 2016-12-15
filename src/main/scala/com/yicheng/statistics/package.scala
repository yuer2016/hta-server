package com.yicheng

import com.typesafe.config.{Config, ConfigList}
import scala.collection.JavaConversions._
import scala.concurrent.duration._

/**
  * Created by yuer on 2016/11/24.
  */
package object statistics {
  implicit class RichConfig(val underlying: Config) extends AnyVal {
    private def readValue[T](path: String, v: => T): Option[T] = underlying.hasPath(path) match {
      case true => Some(v)
      case _ => None
    }

    def getOptionBoolean(path: String): Option[Boolean] = readValue(path, underlying.getBoolean(path))

    def getOptionString(path: String): Option[String] = readValue(path, underlying.getString(path))

    def getOptionInt(path: String): Option[Int] = readValue(path, underlying.getInt(path))

    def getOptionLong(path: String): Option[Long] = readValue(path, underlying.getLong(path))

    def getOptionDouble(path: String): Option[Double] = readValue(path, underlying.getDouble(path))

    def getOptionDuration(path: String): Option[FiniteDuration] = readValue(path, underlying.getDuration(path).toMillis milliseconds)

    def getOptionList(path: String): Option[ConfigList] = underlying.hasPath(path) match {
      case true => Some(underlying.getList(path))
      case _ => None
    }

    def getOptionStringList(path: String): Option[Seq[String]] = underlying.hasPath(path) match {
      case true => Some(underlying.getStringList(path))
      case _ => None
    }

    def getOptionIntList(path: String): Option[Seq[Int]] = underlying.hasPath(path) match {
      case true => Some(underlying.getIntList(path).map(_.toInt))
      case _ => None
    }

    def getOptionConfig(path: String): Option[Config] = readValue(path, underlying.getConfig(path))
  }

}
