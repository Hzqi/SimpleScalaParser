package com.jackywong.scala.parsing.redbook.json

/**
  * Created by huangziqi on 2019/6/27
  */
object JsonExample {
  def main(args: Array[String]): Unit = {
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

    val res = JsonParser.parse(json)
    println(res)
  }
}
