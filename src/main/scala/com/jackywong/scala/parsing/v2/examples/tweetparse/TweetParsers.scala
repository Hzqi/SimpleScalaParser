package com.jackywong.scala.parsing.v2.examples.tweetparse

import com.jackywong.scala.parsing.v2.{ParseError, Parser}
import com.jackywong.scala.parsing.v2.instances.ParserMethods



/**
  * Created by huangziqi on 2019/6/28
  */
trait OneTweet
case class OneA(ones:List[OneTweet]) extends OneTweet
case class OneB(ones:List[OneTweet]) extends OneTweet
case class OneS(string: String) extends OneTweet

object OneTweetParser{
  def root: Parser[OneTweet] = parserA | parserB | parserS
  def parserA: Parser[OneA] = surround(string("{"),string("}"))(many (root)) map (s => OneA(s))
  def parserB: Parser[OneB] = surround(string("("),string(")"))(many (root)) map (s => OneB(s))
  def parserS: Parser[OneS] = surround(string("<"),string(">"))(
    many(charNotIn(')','}','>','(','{','<'))
  ) map(ss => OneS(ss.mkString("")))


  def main(args: Array[String]): Unit = {
    val str = "{()(){}{}<abcd><1234>}"
    val res = ParserMethods.run(root)(str)
    println(res)
  }
}