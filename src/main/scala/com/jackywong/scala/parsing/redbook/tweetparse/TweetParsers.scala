package com.jackywong.scala.parsing.redbook.tweetparse

import com.jackywong.scala.parsing.redbook.ParseError
import com.jackywong.scala.parsing.redbook.instances.AbstractParser
import com.jackywong.scala.parsing.redbook.instances.AbstractParserTypes.Parser


/**
  * Created by huangziqi on 2019/6/28
  */
trait OneTweet
case class OneA(ones:List[OneTweet]) extends OneTweet
case class OneB(ones:List[OneTweet]) extends OneTweet
case class OneS(string: String) extends OneTweet

class OneTweetParser extends AbstractParser {
  //隐式依赖注入使用
  implicit def tok(s: String): Parser[String] = token(string(s))

  def root: Parser[OneTweet] = parserA | parserB | parserS
  def parserA: Parser[OneA] = surround("{","}")(many (root)) map (s => OneA(s))
  def parserB: Parser[OneB] = surround("(",")")(many (root)) map (s => OneB(s))
  def parserS: Parser[OneS] = surround("<",">")(
    many(charNotIn(')','}','>','(','{','<'))
  ) map(ss => OneS(ss.mkString("")))
}

object OneTweetParser {
  val parser = new OneTweetParser()
  def parse(str:String): Either[ParseError, OneTweet] = parser.run(parser.root)(str)

  def main(args: Array[String]): Unit = {
    val str = "{()(){}{}<abcd><1234>}"
    val res = parse(str)
    println(res)
  }
}