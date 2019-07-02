package com.jackywong.scala.parsing.redbook.json


/**
  * Created by huangziqi on 2019/6/27
  */
sealed trait JValue
case class JObj(vals: Map[String,JValue]) extends JValue
case class JAry(vals: List[JValue]) extends JValue
case class JString(value:String) extends JValue
case class JNumber(value:Double) extends JValue
case class JBool(value:Boolean) extends JValue
case object JNull extends JValue