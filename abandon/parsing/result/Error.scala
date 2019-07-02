package com.jackywong.scala.parsing.result

import com.jackywong.scala.parsing.input.Reader

/**
  * Created by huangziqi on 2019/6/26
  */
case class Error[Elem](override val msg: String, override val next: Reader[Elem]) extends NoSuccess(msg, next) {
  /** The toString method of an Error yields an error message. */
  override def toString = s"[${next.pos}] error: $msg\n\n${next.pos.longString}"
  def append[U >: Nothing](a: => ParseResult[U,Elem]): ParseResult[U,Elem] = this
}