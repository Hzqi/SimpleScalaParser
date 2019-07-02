package com.jackywong.scala.parsing.my.examples.json

import com.jackywong.scala.parsing.my.instances.ParserMethods

/**
  * Created by huangziqi on 2019/6/27
  */
object JsonExample {
  val json =
    """
        {
          "name"     :"jacky",
          "age":123,
          "gender":true,
          "object":{
            "aaa":"aaa",
            "bbb":"bbb"
          },
          "arrays":[1,   2,   3,     4     ],
          "nulls": null,
          "str":"abc\"aaa\"",
          "cn_name":"\u9ec4\u5b50\u5947"
        }
      """.stripMargin
  def main(args: Array[String]): Unit = {
    val a = System.currentTimeMillis()
    val res = ParserMethods.run(JsonParser.root)(json)
    val b = System.currentTimeMillis()
    println(s"$res : \n${b-a}ms")
  }
}
