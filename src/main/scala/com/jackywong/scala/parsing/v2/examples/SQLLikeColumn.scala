package com.jackywong.scala.parsing.v2.examples

import com.jackywong.scala.parsing.v2.instances.ParserMethods.run
import com.jackywong.scala.parsing.v2.Helpers._
/**
  * Created by huangziqi on 2019/7/9
  */
object SQLLikeColumn {
  def column = p_str ~ string(".") ~ p_str ~ p_alias.opt <* eof map{
    case a ~ b ~ c ~ d =>
      val head = s"before dot: $a, after dot: $c, "
      d match {
        case None => head + "no alias"
        case Some(None ~ ali) => head + s"alias: $ali"
        case Some(Some(_) ~ ali) => head + s"alias with as: $ali"
      }
  }

  def p_str= token("[A-z]*".r) map(i => i.mkString(""))
  def p_alias = p_alias_as.opt ~ p_str
  def p_alias_as = token(string("as"))

  def main(args: Array[String]): Unit = {
    val s1 = "aaa.bbb as ccc"
    val s2 = "aaa.bbb ccc"
    val s3 = "aaa.bbb"
    val res1 = run(column)(s1)
    val res2 = run(column)(s2)
    val res3 = run(column)(s3)
    println(res1)
    println(res2)
    println(res3)

    //It must be <beforeDot><dot><afterDot><whitespace>[[<as>]<whitespace><alias>]
    //or it will fail.
    val s4 = "aaa bbb ccc"
    val s5 = "aaa.bbb ass ddd"
    val res4 = run(column)(s4)
    val res5 = run(column)(s5)
    println(res4)
    println(res5)
  }
}
