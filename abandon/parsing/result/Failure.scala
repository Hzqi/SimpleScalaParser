package com.jackywong.scala.parsing.result

import com.jackywong.scala.parsing.input.Reader

/**
  * Created by huangziqi on 2019/6/26
  */
case class Failure[Elem](override val msg: String, override val next: Reader[Elem]) extends NoSuccess[Elem](msg, next) {
  /** The toString method of a Failure yields an error message. */
  override def toString = s"[${next.pos}] failure: $msg\n\n${next.pos.longString}"

  def append[U >: Nothing](a: => ParseResult[U,Elem]): ParseResult[U,Elem] = { val alt = a; alt match {
    case Success(_, _) => alt
    case ns: NoSuccess[Elem] => if (alt.next.pos < next.pos) this else alt
  }}
}
