package com.jackywong.scala.parsing.redbook.instances

import com.jackywong.scala.parsing.redbook.{Location, ParseError, Parsers}
import com.jackywong.scala.parsing.redbook.instances.AbstractParserTypes._

import scala.util.matching.Regex

/**
  * Created by huangziqi on 2019/6/27
  */
class AbstractParser extends Parsers[Parser] {

  def run[A](p: Parser[A])(s: String): Either[ParseError,A] = {
    val s0 = ParseState(Location(s))
    p(s0).extract
  }

  /*
    以下的其实每一个都是函数实现，这些实现是父类还没有实现的
    没有实现的说明父类的函数是由这些组合而成
   */

  // consume no characters and succeed with the given value
  def succeed[A](a: A): Parser[A] = s => Success(a, 0)

  def and[A](p: Parser[A], p2: => Parser[A]): Parser[A] =
    flatMap(p)(_ => p2)

  def or[A](p: Parser[A], p2: => Parser[A]): Parser[A] =
    s => p(s) match {
      case Failure(e,false) => p2(s)
      case r => r // committed failure or success skips running `p2`
    }

  def flatMap[A,B](f: Parser[A])(g: A => Parser[B]): Parser[B] =
    s => f(s) match {
      case Success(a,n) => g(a)(s.advanceBy(n))
        .addCommit(n != 0)
        .advanceSuccess(n)
      case f@Failure(_,_) => f
    }

  def string(w: String): Parser[String] = {
    val msg = "'" + w + "'"
      s => {
        val i = AbstractParserTypes.firstNonmatchingIndex(s.loc.input, w, s.loc.offset)
        if (i == -1) // they matched
          Success(w, w.length)
        else
          Failure(s.loc.advanceBy(i).toError(msg), i != 0)
      }
  }

  def charIn(chars:Char*): Parser[Char] = {
    s => {
      val offset = s.loc.offset
      val char = s.loc.input.substring(offset).charAt(0)
      if(chars.contains(char))
        Success(char,1)
      else
        Failure(s.loc.advanceBy(s.loc.col).toError(s"excepted $char"), isCommitted = false)
    }
  }

  def charNotIn(chars:Char*): Parser[Char] = {
    s => {
      val offset = s.loc.offset
      val char = s.loc.input.substring(offset).charAt(0)
      if(!chars.contains(char))
        Success(char,1)
      else
        Failure(s.loc.advanceBy(s.loc.col).toError(s"not excepted $char"), isCommitted = false)
    }
  }

  /* note, regex matching is 'all-or-nothing':
   * failures are uncommitted */
  def regex(r: Regex): Parser[String] = {
    val msg = "regex " + r
    s => r.findPrefixOf(s.input) match {
      case None => Failure(s.loc.toError(msg), false)
      case Some(m) => Success(m,m.length)
    }
  }

  def scope[A](msg: String)(p: Parser[A]): Parser[A] =
    s => p(s).mapError(_.push(s.loc,msg))

  def label[A](msg: String)(p: Parser[A]): Parser[A] =
    s => p(s).mapError(_.label(msg))

  def fail[A](msg: String): Parser[A] =
    s => Failure(s.loc.toError(msg), true)

  def attempt[A](p: Parser[A]): Parser[A] =
    s => p(s).uncommit

  def slice[A](p: Parser[A]): Parser[String] =
    s => p(s) match {
      case Success(_,n) => Success(s.slice(n),n)
      case f@Failure(_,_) => f
    }

  /* We provide an overridden version of `many` that accumulates
   * the list of results using a monolithic loop. This avoids
   * stack overflow errors for most grammars.
   */
  override def many[A](p: Parser[A]): Parser[List[A]] =
    s => {
      var nConsumed: Int = 0
      val buf = new collection.mutable.ListBuffer[A]
      def go(p: Parser[A], offset: Int): Result[List[A]] = {
        p(s.advanceBy(offset)) match {
          case Success(a,n) => buf += a; go(p, offset+n)
          case f@Failure(e,true) => f
          case Failure(e,_) => Success(buf.toList,offset)
        }
      }
      go(p, 0)
    }
}
