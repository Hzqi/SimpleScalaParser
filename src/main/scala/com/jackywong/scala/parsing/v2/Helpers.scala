package com.jackywong.scala.parsing.v2

/**
  * Created by huangziqi on 2019/7/9
  */
object Helpers {
  case class ~[+a, +b](_1: a, _2: b) {
    override def toString = s"(${_1}~${_2})"
  }
}
