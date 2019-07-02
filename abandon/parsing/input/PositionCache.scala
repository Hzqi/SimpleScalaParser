package com.jackywong.scala.parsing.input

import java.util.WeakHashMap

/**
  * Created by huangziqi on 2019/6/27
  */
private[input] trait PositionCache {
  private lazy val indexCacheTL =
  // not DynamicVariable as that would share the map from parent to child :-(
    new ThreadLocal[java.util.Map[CharSequence, Array[Int]]] {
      override def initialValue = new WeakHashMap[CharSequence, Array[Int]]
    }

  private[input] def indexCache = indexCacheTL.get
}
