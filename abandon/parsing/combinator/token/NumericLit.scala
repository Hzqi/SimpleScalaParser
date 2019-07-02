package com.jackywong.scala.parsing.combinator.token

/**
  * Created by huangziqi on 2019/6/27
  */

//数字字面量的token
case class NumericLit(chars: String) extends Token {
  override def toString = chars
}
