package com.jackywong.scala.parsing.my

import com.jackywong.scala.parsing.my.Helpers.~

/**
  * Created by huangziqi on 2019/7/1
  * 每一个Parser仅仅就是一个需要ParseState并且返回Result的函数而已
  */
trait Parser[+A] {
  def exec(state:ParseState): ParseResult[A]

  def label(msg: String): Parser[A]
  def scope(msg: String): Parser[A]
  def attempt: Parser[A]
  def slice: Parser[String]

  def flatMap[B](f: A => Parser[B]): Parser[B]
  def map[B](f: A => B): Parser[B]
  def map2[B,C](p2: => Parser[B])(f: (A,B) => C): Parser[C]
  def product[B](p2: => Parser[B]): Parser[(A,B)]
  def **[B](p2: => Parser[B]): Parser[(A, B)] = product(p2)
  def productWith[B](p2: => Parser[B]): Parser[A~B]
  def ~[B](p2: => Parser[B]): Parser[A~B] = productWith(p2)
  def as[B](b: B): Parser[B]

  //解析的逻辑运算，组合子
  //且 this & that
  def and[B>:A](p2: => Parser[B]): Parser[B]
  def &[B>:A](p2: => Parser[B]): Parser[B] = and(p2)
  //或 this | that
  def or[B>:A](p2: => Parser[B]): Parser[B]
  def |[B>:A](p2: => Parser[B]): Parser[B] = or(p2)
  //省略自身 this *> that
  def skipL[B](p2: => Parser[B]): Parser[B]
  def *>[B](p2: => Parser[B]): Parser[B] = skipL(p2)
  //省略下一个 this <* that
  def skipR(p2: => Parser[_]): Parser[A]
  def <*(p2: => Parser[_]): Parser[A] = skipR(p2)
  //获取option
  def opt: Parser[Option[A]]
  //以p2分割，重复出现零次或多次
  def sep(p2: Parser[_]): Parser[List[A]]
  //以p2分割，重复出现一次或多次
  def sep1(p2: Parser[_]): Parser[List[A]]
  //重复N次
  def listOfN(n: Int): Parser[List[A]]
}
