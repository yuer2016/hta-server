package com.yicheng.statistics.common

import java.lang.reflect.Field

import scala.collection.{GenIterable, GenTraversable, Map}
import scala.util.{Random, Try}

/**
  * Created by yuer on 2016/12/16.
  */
/**
  * 常用工具类
  */
object CommonUtils {

  /**
    * 支持字段名输出信息的接口类
    */
  trait PrettyPrint {
    override def toString: String = prettyPrint(this)
  }

  /**
    * 带字段名输出任何对象信息
    * @param any 任何对象
    * @param literal 字符串是否输出为原始状态
    * @return 输出字符串
    */
  def prettyPrint(any: Any, literal: Boolean = true): String = {
    // Recursively get all the fields; this will grab vals declared in parents of case classes.
    def getFields(cls: Class[_]): List[Field] = Option(cls.getSuperclass).map(getFields).getOrElse(Nil) ++
      cls.getDeclaredFields.toList.filterNot(f => f.isSynthetic || java.lang.reflect.Modifier.isStatic(f.getModifiers))
    // Pretty print any value
    any match {
      // Make Strings look similar to their literal form.
      case s: String =>
        if (literal) '"' + Seq("\n" -> "\\n", "\r" -> "\\r", "\t" -> "\\t", "\"" -> "\\\"", "\\" -> "\\\\").foldLeft(s) { case (acc, (c, r)) => acc.replace(c, r) } + '"'
        else s
      case xs: Array[_] =>
        xs.map(it => prettyPrint(it, literal)).mkString("Array(", ",", ")")
      case xs: Map[_, _] =>
        xs.map(it => prettyPrint(it._1, literal) + " -> " + prettyPrint(it._2, literal)).mkString(xs.stringPrefix + "(", ", ", ")")
      case xs: GenTraversable[_] =>
        xs.map(it => prettyPrint(it, literal)).mkString(xs.stringPrefix + "(", ", ", ")")
      case o: Some[_] =>
        o.map(it => prettyPrint(it, literal)).toString
      case l: Left[_, _] =>
        l.left.map(it => prettyPrint(it, literal)).toString
      case r: Right[_, _] =>
        r.right.map(it => prettyPrint(it, literal)).toString
      // This covers case classes.
      case p: Product =>
        if (!p.isInstanceOf[PrettyPrint] && p.getClass.getMethod("toString") != null) p.toString
        else getFields(p.getClass).map(f => { f.setAccessible(true); f.getName + "=" + prettyPrint(f.get(p), literal) }).mkString(p.productPrefix + '(', ",", ")")
      // General objects and primitives end up here.
      case q =>
        Option(q).map(_.toString).getOrElse("null")
    }
  }

  /**
    * 支持字段名输出信息的接口类
    */
  trait ReallyEquals {
    override def equals(obj: scala.Any): Boolean = reallyEquals(this, obj)
  }

  /**
    * 比较两个对象是否内容相同，即Array/Map/List/Case class的内容是否相同。
    * @param a 对象A
    * @param b 对象B
    * @return true - 两个对象内容相同；false - 两个对象内容不同
    */
  def reallyEquals(a: Any, b: Any): Boolean = {
    (a, b) match {
      case (null, x) => x == null
      case (x, null) => x == null
      case (xa: Array[_], xb: Array[_]) => !xa.zip(xb).exists(it => !reallyEquals(it._1, it._2))
      case (xa: GenIterable[_], xb: GenIterable[_]) => !xa.zip(xb).exists(it => !reallyEquals(it._1, it._2))
      case (xa: Product, xb: Product) => !xa.productIterator.zip(xb.productIterator).exists(it => !reallyEquals(it._1, it._2))
      case (xa: Product, xb) => false
      case (xa, xb: Product) => false
      case (xa, xb) => xa == xb
    }
  }

  /**
    * 将二进制数据转换成Hex字符串
    * @param bin 二进制数据
    * @param sep 分隔符
    * @return Hex字符串
    */
  def bytes2Hex(bin: Array[Byte], sep: String = ""): String = {
    val map = "0123456789ABCDEF"
    Option(bin).map(_.map { b => String.valueOf(Array(map((b >> 4) & 0x0F), map(b & 0x0F))) }.mkString(sep)).orNull
  }

  /**
    * 将Hex字符串转化成二进制数据
    * @param src Hex字符串
    * @param sep 分隔符
    * @return 二进制数据
    */
  def hex2Bytes(src: String, sep: String = ""): Array[Byte] = {
    Try(Range(0, src.length, 2 + sep.length).map(i => Integer.parseInt(src.substring(i, i + 2), 16).toByte).toArray).getOrElse(null)
  }

  def genRandomString(size: Int): String = {
    val base = "abcdefghijklmnopqrstuvwxyz0123456789"
    (for (_ <- 1 to size) yield base.charAt(Random.nextInt(base.length))).mkString
  }
}
