package com.jackywong.scala.parsing.v2

/**
  * Created by huangziqi on 2019/7/1
  */
case class ParseState(loc: Location) {
  def advanceBy(numChars: Int): ParseState =
    copy(loc = loc.copy(offset = loc.offset + numChars))
  def input: String = loc.input.substring(loc.offset)
  def slice(n: Int) = loc.input.substring(loc.offset, loc.offset + n)
}
