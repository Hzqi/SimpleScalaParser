package com.jackywong.scala.parsing.redbook.json

import com.jackywong.scala.parsing.redbook.ParseError
import com.jackywong.scala.parsing.redbook.instances.AbstractParser
import com.jackywong.scala.parsing.redbook.instances.AbstractParserTypes.Parser

/**
  * Created by huangziqi on 2019/6/27
  */
class JsonParser extends AbstractParser {

  //隐式依赖注入使用
  implicit def tok(s: String): Parser[String] = token(string(s))

  def root: Parser[JValue] = whitespace *> (jObj | jArr)

  def jObj: Parser[JObj] = surround( "{","}")((keyval <* whitespace) sep "," map (kvs => JObj(kvs.toMap))) scope "object"

  def jArr: Parser[JAry] = surround("[","]")((value <* whitespace) sep "," map (vs => JAry(vs))) scope "array"

  def keyval:Parser[(String,JValue)] = quoted ** (whitespace *> ":" *> value)

  def lit:Parser[JValue] = scope("literal") {
    "null".as(JNull) |
      double.map(JNumber(_)) |
      escapedQuoted.map(JString(_)) |
      "true".as(JBool(true)) |
      "false".as(JBool(false))
  }

  def value: Parser[JValue] = lit | jObj | jArr
}

object JsonParser {
  val parser = new JsonParser()

  def parse(json:String): Either[ParseError, JValue] = parser.run(parser.root)(json)
}