package com.jackywong.scala.parsing.combinator.token

/**
  * Created by huangziqi on 2019/6/27
  */

//关键字的token
case class Keyword(chars: String) extends Token {
  override def toString = s"'$chars'"
}