package com.jackywong.scala.parsing.my.instances

import java.util.regex.Pattern
import com.jackywong.scala.parsing.my._
import scala.util.matching.Regex

/**
  * Created by huangziqi on 2019/7/1
  */
object AbstractParsers extends Parsers {
  override def defaultSucceed[A](a: A): Parser[A] = string("") map (_ => a)

  override def succeed[A](a: A): Parser[A] = AbstractParser(
    s => Success(a,0)
  )

  override implicit def string(s: String): Parser[String] = AbstractParser{
    val msg = "'" + s + "'"
    state => {
      val i = ParserMethods.firstNonmatchingIndex(state.loc.input, s, state.loc.offset)
      if (i == -1) // they matched
        Success(s, s.length)
      else
        Failure(state.loc.advanceBy(i).toError(msg), i != 0)
    }
  }

  override def char(c: Char): Parser[Char] = string(c.toString) map (_.charAt(0))

  //for java
  def pchar(c:Char): Parser[Char] = char(c)

  override def charIn(chars: Char*): Parser[Char] = AbstractParser(
    s => {
      val offset = s.loc.offset
      val char = s.loc.input.substring(offset).charAt(0)
      if(chars.contains(char))
        Success(char,1)
      else
        Failure(s.loc.advanceBy(s.loc.col).toError(s"excepted $char"), isCommitted = false)
    }
  )

  override def charNotIn(chars: Char*): Parser[Char] = AbstractParser(
    s => {
      val offset = s.loc.offset
      val char = s.loc.input.substring(offset).charAt(0)
      if(!chars.contains(char))
        Success(char,1)
      else
        Failure(s.loc.advanceBy(s.loc.col).toError(s"not excepted $char"), isCommitted = false)
    }
  )

  override def many1[A](p: Parser[A]): Parser[List[A]] = p.map2(many(p))(_ :: _)

  override def many[A](p: Parser[A]): Parser[List[A]] = p.map2(many(p))(_ :: _) or succeed(List())

  override implicit def regex(r: Regex): Parser[String] = AbstractParser{
    val msg = "regex " + r
    s => r.findPrefixOf(s.input) match {
      case None => Failure(s.loc.toError(msg), false)
      case Some(m) => Success(m,m.length)
    }
  }


  override def whitespace: Parser[String] = regex("\\s*".r)

  override def digits: Parser[String] = regex("\\d+".r)

  override def thru(s: String): Parser[String] = regex((".*?"+Pattern.quote(s)).r)

  override def quoted: Parser[String] = string("\"") *> thru("\"").map(_.dropRight(1))

  override def escaped: Parser[String] =
    (char('\\') and char('\"') map(_ => "\"")) |
    (char('\\') and char('\\') map(_ => "\\")) |
    (char('\\') and char('/') map(_ => "/")) |
    (char('\\') and char('b') map(_ => "\b")) |
    (char('\\') and char('f') map(_ => "\f")) |
    (char('\\') and char('n') map(_ => "\n")) |
    (char('\\') and char('r') map(_ => "\r")) |
    (char('\\') and char('t') map(_ => "\t"))

  override def escapedQuoted: Parser[String] =
    surround(string("\""),string("\""))(many1(escaped or charNotIn('\"', '\\')))
      .map(ss => ss.mkString(""))

  override def doubleString: Parser[String] = token(regex("[-+]?([0-9]*\\.)?[0-9]+([eE][-+]?[0-9]+)?".r))

  override def double: Parser[Double] = doubleString map (_.toDouble) label "double literal"

  //for java
  def pdouble: Parser[Double] = double

  override def token[A](p: Parser[A]): Parser[A] = whitespace *> p.attempt <* whitespace

  override def opL[A](p: Parser[A])(op: Parser[(A, A) => A]): Parser[A] =
    p.map2(many(op ** p))((h,t) => t.foldLeft(h)((a,b) => b._1(a,b._2)))

  override def surround[A](start: Parser[_], stop: Parser[_])(p: => Parser[A]): Parser[A] = start *> p <* stop

  override def eof: Parser[String] = regex("\\z".r).label("unexpected trailing characters")

  override def root[A](p: Parser[A]): Parser[A] = p <* eof
}
