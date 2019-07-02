package com.jackywong.scala.parsing.my.examples.json

import com.jackywong.scala.parsing.my.Parser
import com.jackywong.scala.parsing.my.instances.AbstractParsers._

/**
  * Created by huangziqi on 2019/7/1
  */
object JsonParser {
  def root: Parser[JValue] = whitespace *> (jObj | jArr)

  def jObj: Parser[JObj] = surround( string("{"), string("}")) (token(keyval) sep string(",") map (kvs => JObj(kvs.toMap))) scope "object"

  def jArr: Parser[JAry] = surround(string("["), string("]")) (token(value) sep string(",") map (vs => JAry(vs))) scope "array"

  def keyval:Parser[(String,JValue)] = quoted **  (token(string(":")) *> value)

  def lit:Parser[JValue] =  {
    string("null").as(JNull) |
      double.map(JNumber) |
      escapedQuoted.map(JString) |
      string("true").as(JBool(true)) |
      string("false").as(JBool(false))
  }.scope("literal")

  def value: Parser[JValue] = lit | jObj | jArr
}
