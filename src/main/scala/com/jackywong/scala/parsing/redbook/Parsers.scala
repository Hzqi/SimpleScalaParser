package com.jackywong.scala.parsing.redbook

import java.util.regex.Pattern

import scala.util.matching.Regex

/**
  * Created by huangziqi on 2019/6/27
  */
trait Parsers[Parser[+_]] { self =>
  //运行parser，计算结果
  def run[A](p: Parser[A])(input: String): Either[ParseError,A]

  /*
    下面三个函数是描述Parser的特征，即Parser应该具有哪些特征
    label：一些标签，与name一样，描述这个Parser需要是什么
    scope：范围，报错时报错信息中的什么范围
    attempt：尝试，尝试执行的parser组合子
   */
  def label[A](msg: String)(p: Parser[A]): Parser[A]
  def scope[A](msg: String)(p: Parser[A]): Parser[A]
  def attempt[A](p: Parser[A]): Parser[A]
  //默认成功值
  def defaultSucceed[A](a: A): Parser[A] = string("") map (_ => a)
  def succeed[A](a: A): Parser[A]
  //切片
  def slice[A](p: Parser[A]): Parser[String]


  //隐式转字符串为字符串解析器
  implicit def string(s: String): Parser[String]
  //字符解析器
  def char(c: Char): Parser[Char] = string(c.toString) map (_.charAt(0))
  def charIn(chars:Char*): Parser[Char]
  def charNotIn(chars:Char*):Parser[Char]

  /*
    一下两个是隐式转换性函数，
    1、用于将Parser转为ParserOps，使用中序
    2、将任意类型转为Parser[String]
   */
  //隐式转parser为parser操作符
  //parserOps 具有中序操作符，但其实实现的方式都是Parsers的，所以这个函数是直接转的
  implicit def operators[A](p: Parser[A]): ParserOps[A] = ParserOps[A](p)
  implicit def asStringParser[A](a: A)(implicit f: A => Parser[String]): ParserOps[String] = ParserOps(f(a))


  /*
    高阶性质的函数
   */
  def flatMap[A,B](p: Parser[A])(f: A => Parser[B]): Parser[B]
  def map[A,B](a: Parser[A])(f: A => B): Parser[B] = flatMap(a)(f andThen succeed)
  def map2[A,B,C](p: Parser[A], p2: => Parser[B])(f: (A,B) => C): Parser[C] =
    for { a <- p; b <- p2 } yield f(a,b)

  def product[A,B](p: Parser[A], p2: => Parser[B]): Parser[(A,B)] =
    flatMap(p)(a => map(p2)(b => (a,b)))

  /*
    一下开始就是Parser的操作函数
   */

  //多个
  def many1[A](p: Parser[A]): Parser[List[A]] = map2(p, many(p))(_ :: _)

  //重复N次
  def listOfN[A](n: Int, p: Parser[A]): Parser[List[A]] =
    if (n <= 0) succeed(List())
    else map2(p, listOfN(n-1, p))(_ :: _)

  //0个或多个？
  def many[A](p: Parser[A]): Parser[List[A]] =
    map2(p, many(p))(_ :: _) or succeed(List())

  //且
  def and[A](p1: Parser[A], p2: => Parser[A]): Parser[A]
  //或
  def or[A](p1: Parser[A], p2: => Parser[A]): Parser[A]

  implicit def regex(r: Regex): Parser[String]

  /** Sequences two parsers, ignoring the result of the first.
    * We wrap the ignored half in slice, since we don't care about its result. */
  //解析读取两个Parser，忽略第一个parser的结果
  def skipL[B](p: Parser[Any], p2: => Parser[B]): Parser[B] =
    map2(slice(p), p2)((_,b) => b)

  /** Sequences two parsers, ignoring the result of the second.
    * We wrap the ignored half in slice, since we don't care about its result. */
  //解析读取两个Parser，忽略第二个parser的结果
  def skipR[A](p: Parser[A], p2: => Parser[Any]): Parser[A] =
    map2(p, slice(p2))((a,b) => a)

  //
  def opt[A](p: Parser[A]): Parser[Option[A]] =
    p.map(Some(_)) or succeed(None)

  /** Parser which consumes zero or more whitespace characters. */
  //解析0个或多个空白格
  def whitespace: Parser[String] = "\\s*".r

  /** Parser which consumes 1 or more digits. */
  //解析1个或多个数字的字符串
  def digits: Parser[String] = "\\d+".r

  /** Parser which consumes reluctantly until it encounters the given string. */
  //读取任意字符直到想要的为止
  def thru(s: String): Parser[String] = (".*?"+Pattern.quote(s)).r

  /** Unescaped string literals, like "foo" or "bar". */
  //未转义的字符串文字，如“foo”或“bar”。
  def quoted: Parser[String] = string("\"") *> thru("\"").map(_.dropRight(1))

  def escaped: Parser[String] =
    (char('\\') and char('\"') map(_ => "\"")) |
      (char('\\') and char('\\') map(_ => "\\")) |
      (char('\\') and char('/') map(_ => "/")) |
      (char('\\') and char('b') map(_ => "\b")) |
      (char('\\') and char('f') map(_ => "\f")) |
      (char('\\') and char('n') map(_ => "\n")) |
      (char('\\') and char('r') map(_ => "\r")) |
      (char('\\') and char('t') map(_ => "\t"))


  /** Unescaped or escaped string literals, like "An \n important \"Quotation\"" or "bar". */
  //转义的或未转义的字符串 （没有源码，被练习了）
  def escapedQuoted: Parser[String] =
  // rather annoying to write, left as an exercise
  // we'll just use quoted (unescaped literals) for now
//    token(quoted label "string literal")
  surround("\"","\"")(many1(escaped or charNotIn('\"', '\\'))) map(ss => ss.mkString(""))

  /** C/Java style floating point literals, e.g .1, -1.0, 1e9, 1E-23, etc.
    * Result is left as a string to keep full precision
    */
  //浮点数字符串
  def doubleString: Parser[String] =
    token("[-+]?([0-9]*\\.)?[0-9]+([eE][-+]?[0-9]+)?".r)

  /** Floating point literals, converted to a `Double`. */
  //浮点数
  def double: Parser[Double] =
    doubleString map (_.toDouble) label "double literal"

  /** Attempts `p` and strips trailing whitespace, usually used for the tokens of a grammar. */
  //读取token
  def token[A](p: Parser[A]): Parser[A] =
    attempt(p) <* whitespace

  /** Zero or more repetitions of `p`, separated by `p2`, whose results are ignored. */
  //“p”的零次或多次重复，由“p2”分隔，其结果被忽略。
  def sep[A](p: Parser[A], p2: Parser[Any]): Parser[List[A]] = // use `Parser[Any]` since don't care about result type of separator
    sep1(p,p2) or succeed(List())

  /** One or more repetitions of `p`, separated by `p2`, whose results are ignored. */
  //“p”的一次或多次重复，由“p2”分隔，其结果被忽略。
  def sep1[A](p: Parser[A], p2: Parser[Any]): Parser[List[A]] =
    map2(p, many(p2 *> p))(_ :: _)

  /** Parses a sequence of left-associative binary operators with the same precedence. */
  //解析具有相同优先级的左关联二元运算符序列。
  def opL[A](p: Parser[A])(op: Parser[(A,A) => A]): Parser[A] =
    map2(p, many(op ** p))((h,t) => t.foldLeft(h)((a,b) => b._1(a,b._2)))

  /** Wraps `p` in start/stop delimiters. */
  //p包含在start、stop中
  def surround[A](start: Parser[Any], stop: Parser[Any])(p: => Parser[A]): Parser[A] =
    start *> p <* stop

  /** A parser that succeeds when given empty input. */
  def eof: Parser[String] =
    regex("\\z".r).label("unexpected trailing characters")

  /** The root of the grammar, expects no further input following `p`. */
  def root[A](p: Parser[A]): Parser[A] =
    p <* eof


  /**
    * 这个内部类仅仅是用于中序操作符（中序函数）的
    * 若在Haskell上直接可以去掉，直接使用中序
    * @param p
    * @tparam A
    */
  case class ParserOps[A](p: Parser[A]) {
    def |[B>:A](p2: => Parser[B]): Parser[B] = self.or(p,p2) // use `self` to explicitly disambiguate reference to the `or` method on the `trait`
    def or[B>:A](p2: => Parser[B]): Parser[B] = self.or(p,p2)
    def &[B>:A](p2: => Parser[B]): Parser[B] = self.and(p,p2)
    def and[B>:A](p2: => Parser[B]): Parser[B] = self.and(p,p2)
    def map[B](f: A => B): Parser[B] = self.map(p)(f)
    def many: Parser[List[A]] = self.many(p)
    def slice: Parser[String] = self.slice(p)
    def **[B](p2: => Parser[B]): Parser[(A,B)] = self.product(p,p2)
    def product[B](p2: => Parser[B]): Parser[(A,B)] = self.product(p,p2)
    def flatMap[B](f: A => Parser[B]): Parser[B] = self.flatMap(p)(f)
    def label(msg: String): Parser[A] = self.label(msg)(p)
    def scope(msg: String): Parser[A] = self.scope(msg)(p)
    def *>[B](p2: => Parser[B]): Parser[B] = self.skipL(p, p2)
    def <*(p2: => Parser[Any]): Parser[A] = self.skipR(p, p2)
    def token: Parser[A] = self.token(p)
    def sep(separator: Parser[Any]): Parser[List[A]] = self.sep(p, separator)
    def sep1(separator: Parser[Any]): Parser[List[A]] = self.sep1(p, separator)
    def as[B](b: B): Parser[B] = self.map(self.slice(p))(_ => b)
    def opL(op: Parser[(A,A) => A]): Parser[A] = self.opL(p)(op)
  }
}
