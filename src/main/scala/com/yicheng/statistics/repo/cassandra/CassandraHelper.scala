package com.yicheng.statistics.repo.cassandra

import com.datastax.driver.core.{ResultSet, Row, Session, Statement}
import com.google.common.util.concurrent.{AsyncFunction, FutureCallback, Futures, ListenableFuture}

import scala.concurrent.{Future, Promise}
import CassandraDB._

/**
  * Created by yuer on 2016/11/24.
  * 将google Guava 封装 java concurrent Future 转换为 scala concurrent
  */
object CassandraHelper {

  implicit class RichListenableFuture[T](lf: ListenableFuture[T]){
    def asScala: Future[T] = {
      val p = Promise[T]()
      Futures.addCallback(lf, new FutureCallback[T] {
        def onFailure(t: Throwable): Unit = p failure t
        def onSuccess(result: T): Unit = p success result
      })
      p.future
    }
    def one: Future[Option[Row]] = {
      val p = Promise[Option[Row]]()
      Futures.addCallback(lf, new FutureCallback[T] {
        def onFailure(t: Throwable): Unit = p failure t
        def onSuccess(result: T): Unit = p success Option(result.asInstanceOf[ResultSet].one())
      })
      p.future
    }
  }
  def executeAsync(query: Statement): ListenableFuture[ResultSet] = Futures.transform(session,
    new AsyncFunction[Session, ResultSet]() {
      def apply(session: Session) = {
        session.executeAsync(query)
      }
    }
  )
}
