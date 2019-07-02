# SimpleScalaParser

这是一个参考（修改）《Scala函数式编程》第九章所写出来的Scala的解析器组合器。


主要的目录是`com.jackywong.scala.parsing.my`，其他目录为标准库的目录，或者是原生的目录。

## Usage
通过简单组合的Parser，然后运行。如下是组合简单的解析器而构成的大解析器

```
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
```

如下是运行:

```
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
          "str":"abc\"aaa\""
        }
      """.stripMargin
  def main(args: Array[String]): Unit = {
    val res = ParserMethods.run(JsonParser.root)(json)
    println(s"$res")
  }
}
```

详细的简单组合子在 `com.jackywong.scala.parsing.my.Parser` 和 `com.jackywong.scala.parsing.my.Parsers`中。