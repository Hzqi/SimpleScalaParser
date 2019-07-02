package com.jackywong.scala.parsing.my

/**
  * Created by huangziqi on 2019/7/1
  * 利用offset去描述不同的位置。offset初始化为0
  */
case class Location(input:String, offset:Int = 0) {
  //记录第几行
  lazy val line : Int = input.slice(0,offset+1).count(_ == '\n') + 1
  //记录第几个
  lazy val col: Int = input.slice(0,offset+1).lastIndexOf('\n') match {
    case -1 => offset + 1
    case lineStart => offset - lineStart
  }

  def toError(msg:String): ParseError = ParseError(List((this, msg)))

  def advanceBy(n: Int): Location = copy(offset = offset+n)

  def currentLine: String =
    if (input.length > 1) input.lines.drop(line-1).next
    else ""

  //提示错误的列
  def columnCaret: String = (" " * (col-1)) + "^"
}
