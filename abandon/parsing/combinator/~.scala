package com.jackywong.scala.parsing.combinator

/**
  * Created by huangziqi on 2019/6/26
  */
case class ~[+a, +b](_1: a, _2: b) {
  override def toString = s"(${_1}~${_2})"
}
