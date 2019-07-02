package com.jackywong.scala.parsing.my

import scala.util.matching.Regex

/**
  * Created by huangziqi on 2019/7/1
  */
trait Parsers {
  def defaultSucceed[A](a: A): Parser[A]
  def succeed[A](a: A): Parser[A]
  def string(s: String): Parser[String]
  def char(c: Char): Parser[Char]
  def charIn(chars:Char*): Parser[Char]
  def charNotIn(chars:Char*):Parser[Char]
  def many1[A](p: Parser[A]): Parser[List[A]]
  def many[A](p: Parser[A]): Parser[List[A]]
  def regex(r: Regex): Parser[String]
  def whitespace: Parser[String]
  def digits: Parser[String]
  def thru(s: String): Parser[String]
  def quoted: Parser[String]
  def escaped: Parser[String]
  def escapedQuoted: Parser[String]
  def doubleString: Parser[String]
  def double: Parser[Double]
  def token[A](p: Parser[A]): Parser[A]
  def opL[A](p: Parser[A])(op: Parser[(A,A) => A]): Parser[A]
  def surround[A](start: Parser[_], stop: Parser[_])(p: => Parser[A]): Parser[A]
  def eof: Parser[String]
  def root[A](p: Parser[A]): Parser[A]
}
