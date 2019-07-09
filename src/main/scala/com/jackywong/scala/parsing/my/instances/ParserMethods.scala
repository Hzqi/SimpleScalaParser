package com.jackywong.scala.parsing.my.instances

import com.jackywong.scala.parsing.my.{Location, ParseError, ParseState, Parser}

/**
  * Created by huangziqi on 2019/7/1
  */
object ParserMethods {
  def firstNonmatchingIndex(s1: String, s2: String, offset: Int): Int = {
    var i = 0
    while (i < s1.length && i < s2.length && (i+offset) < s1.length) {
      if (s1.charAt(i+offset) != s2.charAt(i)) return i
      i += 1
    }
    if (s1.length-offset >= s2.length) -1
    else s1.length-offset
  }

  def run[A](p: Parser[A])(s: String): Either[ParseError,A] = {
    val s0 = ParseState(Location(s))
    p.exec(s0).extract
  }
}
