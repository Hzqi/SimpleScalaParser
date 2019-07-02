package com.jackywong.scala.parsing.combinator.token

/**
  * Created by huangziqi on 2019/6/27
  */

//标识符token
case class Identifier(chars: String) extends Token {
  override def toString = s"identifier $chars"
}
