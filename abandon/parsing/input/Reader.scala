package com.jackywong.scala.parsing.input

/**
  * Created by huangziqi on 2019/6/26
  */
abstract class Reader[+T] {

  def source: java.lang.CharSequence = throw new NoSuchMethodError("not a char sequence reader")

  def offset: Int = throw new NoSuchMethodError("not a char sequence reader")

  def first: T

  def rest: Reader[T]

  def drop(n: Int): Reader[T] = {
    var r: Reader[T] = this
    var cnt = n
    while (cnt > 0) {
      r = r.rest; cnt -= 1
    }
    r
  }

  def pos: Position

  def atEnd: Boolean
}
