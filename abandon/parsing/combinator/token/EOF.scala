package com.jackywong.scala.parsing.combinator.token

/**
  * Created by huangziqi on 2019/6/27
  */
case object EOF extends Token {
  def chars = "<eof>"
}
