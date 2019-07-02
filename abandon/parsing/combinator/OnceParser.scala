package com.jackywong.scala.parsing.combinator

import com.jackywong.scala.parsing.input.Reader
import com.jackywong.scala.parsing.result.ParseResult

/**
  * Created by huangziqi on 2019/6/26
  */
@Deprecated
trait OnceParser[+T,Elem] extends Parser[T,Elem] {
  override def ~ [U](p: => Parser[U,Elem]): Parser[~[T, U],Elem]
  = OnceParser.makeOnceParser{ (for(a <- this; b <- Parser.commit(p)) yield new ~(a,b)).named("~") }

  override def ~> [U](p: => Parser[U,Elem]): Parser[U,Elem]
  = OnceParser.makeOnceParser{ (for(a <- this; b <- Parser.commit(p)) yield b).named("~>") }

  override def <~ [U](p: => Parser[U,Elem]): Parser[T,Elem]
  = OnceParser.makeOnceParser{ (for(a <- this; b <- Parser.commit(p)) yield a).named("<~") }
}

object OnceParser {
  def makeOnceParser[T,Elem](f: Reader[Elem] => ParseResult[T,Elem]): Parser[T,Elem] with OnceParser[T,Elem] =
    new Parser[T,Elem] with OnceParser[T,Elem] {
      def apply(in: Input) = f(in)
    }
}