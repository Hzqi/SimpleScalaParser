package com.jackywong.scala.parsing.redbook

/**
  * Created by huangziqi on 2019/6/27
  */

//位置信息
case class Location(input: String, offset: Int = 0) {

  lazy val line: Int = input.slice(0,offset+1).count(_ == '\n') + 1
  lazy val col: Int = input.slice(0,offset+1).lastIndexOf('\n') match {
    case -1 => offset + 1
    case lineStart => offset - lineStart
  }

  def toError(msg: String): ParseError =
    ParseError(List((this, msg)))

  def advanceBy(n: Int): Location = copy(offset = offset+n)

  /* Returns the line corresponding to this location */
  def currentLine: String =
    if (input.length > 1) input.lines.drop(line-1).next
    else ""

  def columnCaret: String = (" " * (col-1)) + "^"
}
