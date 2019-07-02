package com.jackywong.scala.parsing.combinator.token

/**
  * Created by huangziqi on 2019/6/27
  */
abstract class Token {
  def chars: String
}

object Token {
  def errorToken(msg: String): Token = ErrorToken(msg)
}