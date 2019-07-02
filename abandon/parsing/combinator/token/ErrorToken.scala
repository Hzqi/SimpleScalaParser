package com.jackywong.scala.parsing.combinator.token

/**
  * Created by huangziqi on 2019/6/27
  */
case class ErrorToken(msg: String) extends Token {
  def chars = s"*** error: $msg"
}
