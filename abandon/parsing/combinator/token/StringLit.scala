package com.jackywong.scala.parsing.combinator.token

/**
  * Created by huangziqi on 2019/6/27
  */

//字符串字面量的token
case class StringLit(chars: String) extends Token {
  override def toString = s""""$chars""""
}
