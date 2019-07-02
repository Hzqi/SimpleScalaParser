package com.jackywong.scala.parsing.combinator.lexical

import com.jackywong.scala.parsing.combinator.Parser
import com.jackywong.scala.parsing.input.{CharArrayReader, Reader}
import com.jackywong.scala.parsing.result.{NoSuccess, Success}

/**
  * Created by huangziqi on 2019/6/27
  */
abstract class Scanner[Tok](in: Reader[Char]) extends Reader [Tok]{
  def errorToken(msg: String): Tok
  /** A parser that produces a token (from a stream of characters). */
  def token: Parser[Tok,Char]
  /** A parser for white-space -- its result will be discarded. */
  def whitespace: Parser[Any,Char]
  /**构造对象用的函数 **/
  def newScanner(in: Reader[Char]): Scanner[Tok]


  def this(in: String) = this(new CharArrayReader(in.toCharArray))
  private val (tok, rest1, rest2) = whitespace(in) match {
    case Success(_, in1) =>
      token(in1) match {
        case Success(tok, in2) => (tok, in1, in2)
        case ns: NoSuccess[Char] => (errorToken(ns.msg), ns.next, skip(ns.next))
      }
    case ns: NoSuccess[Char] => (errorToken(ns.msg), ns.next, skip(ns.next))
  }
  private def skip(in: Reader[Char]) = if (in.atEnd) in else in.rest

  override def source: java.lang.CharSequence = in.source
  override def offset: Int = in.offset
  def first = tok
  def rest = newScanner(rest2)
  def pos = rest1.pos
  def atEnd = in.atEnd || (whitespace(in) match { case Success(_, in1) => in1.atEnd case _ => false })
}
