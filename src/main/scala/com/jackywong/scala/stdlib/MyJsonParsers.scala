package com.jackywong.scala.stdlib

import scala.util.parsing.combinator.ImplicitConversions
import scala.util.parsing.combinator.lexical.StdLexical
import scala.util.parsing.combinator.syntactical.StdTokenParsers
import scala.util.parsing.input.CharArrayReader.EofCh

/**
  * Created by huangziqi on 2019/6/25
  */

trait JValue
case class JObj(vals: Map[String,JValue]) extends JValue
case class JAry(vals: List[JValue]) extends JValue
case class JString(value:String) extends JValue
case class JNumber(value:Double) extends JValue
case class JBool(value:Boolean) extends JValue
case object JNull extends JValue

class MyJsonLexer extends StdLexical with ImplicitConversions {

  override def token: Parser[Token] =
  //( '\"' ~ rep(charSeq | letter) ~ '\"' ^^ lift(StringLit)
    ( string ^^ StringLit
      | number ~ letter ^^ { case n ~ l => ErrorToken("Invalid number format : " + n + l) }
      | '-' ~> whitespace ~ number ~ letter ^^ { case ws ~ num ~ l => ErrorToken("Invalid number format : -" + num + l) }
      | '-' ~> whitespace ~ number ^^ { case ws ~ num => NumericLit("-" + num) }
      | number ^^ NumericLit
      | EofCh ^^^ EOF
      | delim
      | '\"' ~> failure("Unterminated string")
      | rep(letter) ^^ checkKeyword
      | failure("Illegal character")
      )

  def checkKeyword(xs : List[Any]) = {
    val strRep = xs mkString ""
    if (reserved contains strRep) Keyword(strRep) else ErrorToken("Not a keyword: " + strRep)
  }

  /** A string is a collection of zero or more Unicode characters, wrapped in
    *  double quotes, using backslash escapes (cf. http://www.json.org/).
    */
  def string = '\"' ~> rep(charSeq | chrExcept('\"', '\n', EofCh)) <~ '\"' ^^ { _ mkString "" }

  override def whitespace = rep(whitespaceChar)

  def number = intPart ~ opt(fracPart) ~ opt(expPart) ^^ { case i ~ f ~ e =>
    i + optString(".", f) + optString("", e)
  }
  def intPart = zero | intList
  def intList = nonzero ~ rep(digit) ^^ {case x ~ y => (x :: y) mkString ""}
  def fracPart = '.' ~> rep(digit) ^^ { _ mkString "" }
  def expPart = exponent ~ opt(sign) ~ rep1(digit) ^^ { case e ~ s ~ d =>
    e + optString("", s) + d.mkString("")
  }

  private def optString[A](pre: String, a: Option[A]) = a match {
    case Some(x) => pre + x.toString
    case None => ""
  }

  def zero: Parser[String] = '0' ^^^ "0"
  def nonzero = elem("nonzero digit", d => d.isDigit && d != '0')
  def exponent = elem("exponent character", d => d == 'e' || d == 'E')
  def sign = elem("sign character", d => d == '-' || d == '+')

  def charSeq: Parser[String] =
    ('\\' ~ '\"' ^^^ "\""
      |'\\' ~ '\\' ^^^ "\\"
      |'\\' ~ '/'  ^^^ "/"
      |'\\' ~ 'b'  ^^^ "\b"
      |'\\' ~ 'f'  ^^^ "\f"
      |'\\' ~ 'n'  ^^^ "\n"
      |'\\' ~ 'r'  ^^^ "\r"
      |'\\' ~ 't'  ^^^ "\t"
      |'\\' ~> 'u' ~> unicodeBlock)

  val hexDigits = Set[Char]() ++ "0123456789abcdefABCDEF".toArray
  def hexDigit = elem("hex digit", hexDigits.contains(_))

  private def unicodeBlock = hexDigit ~ hexDigit ~ hexDigit ~ hexDigit ^^ {
    case a ~ b ~ c ~ d =>
      new String(Array(Integer.parseInt(List(a, b, c, d) mkString "", 16)), 0, 1)
  }

  //private def lift[T](f: String => T)(xs: List[Any]): T = f(xs mkString "")
}

class MyJsonParser extends StdTokenParsers with ImplicitConversions {
  override type Tokens = MyJsonLexer
  override val lexical = new Tokens

  // Configure lexical parsing
  //需要添加关键词
  lexical.reserved ++= List("true","false","null")         //保留字 ? 必须需要添加保留字才能直接对
  lexical.delimiters ++= List("{", "}", "[", "]", ":", ",") //分隔符

  protected val numberParser = new ThreadLocal[String => Double]() {
    override def initialValue() = _.toDouble
  }

  def root = jsonObj | jsonArray
  def jsonObj = "{" ~> rep1sep(objEntry,",") <~ "}" ^^ {case vals:List[(String,JValue)] => JObj(Map(vals:_*))}
  def jsonArray = "[" ~> rep1sep(jsvalue,",") <~ "]" ^^ {case vals:List[JValue] => JAry(vals)}
  def objEntry = pureStringVal ~ (":" ~> jsvalue) ^^ { case x ~ y => (x, y) }
  def jsvalue: Parser[Any] = jsonObj |
    jsonArray |
    jsnumber |
    "true" ^^ {_ => JBool(true)} |
    "false" ^^ {_ => JBool(false)} |
    "null" ^^ {_ => JNull} |
    stringVal
  def pureStringVal = accept("pureString",{case lexical.StringLit(n) => n} )
  def stringVal= accept("string", { case lexical.StringLit(n) => JString(n)} )
  def jsnumber = accept("number", { case lexical.NumericLit(n) => JNumber(numberParser.get.apply(n))} )


  def parseRaw(input : String): ParseResult[JValue] =
    phrase(root)(new lexical.Scanner(input))
}

object Main extends MyJsonParser {
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
    parseRaw(json) match {
      case Success(matched,_) => println(matched)
      case Failure(msg,_) => println(s"FAILURE: $msg")
      case Error(msg,_) => println(s"ERROR: $msg")
    }

    val r = root
    println(r)
  }
}
