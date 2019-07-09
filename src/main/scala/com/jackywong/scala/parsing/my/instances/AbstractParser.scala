package com.jackywong.scala.parsing.my.instances

import com.jackywong.scala.parsing.my.Helpers.~
import com.jackywong.scala.parsing.my._

/**
  * Created by huangziqi on 2019/7/1
  */
case class AbstractParser[+A](execFunc: ParseState => ParseResult[A]) extends Parser[A]{
  override def exec(state: ParseState): ParseResult[A] = execFunc(state)

  override def label(msg: String): Parser[A] = AbstractParser(
    s => exec(s).mapError(_.label(msg))
  )

  override def scope(msg: String): Parser[A] = AbstractParser(
    s => exec(s).mapError(_.push(s.loc,msg))
  )

  override def attempt: Parser[A] = AbstractParser(
    s => exec(s).uncommit
  )

  override def slice: Parser[String] = AbstractParser(
    s => exec(s) match {
      case Success(_,n) => Success(s.slice(n),n)
      case f@Failure(_,_) => f
    }
  )

  override def flatMap[B](g: A => Parser[B]): Parser[B] = AbstractParser(
    s => exec(s) match {
      case Success(a,n) => g(a).exec(s.advanceBy(n))
        .addCommit(n != 0)
        .advanceSuccess(n)
      case f@Failure(_,_) => f
    }
  )

  override def map[B](f: A => B): Parser[B] = flatMap(f andThen AbstractParsers.succeed )

  override def map2[B, C](p2: => Parser[B])(f: (A, B) => C): Parser[C] =
    for { a <- this; b <- p2 } yield f(a,b)

  override def product[B](p2: => Parser[B]): Parser[(A, B)] = flatMap(a => p2.map(b => (a,b)))

  override def productWith[B](p2: => Parser[B]): Parser[~[A, B]] = flatMap(a => p2.map(b => new ~(a,b)))

  override def as[B](b: B): Parser[B] = slice.map(_ => b)

  override def and[B >: A](p2: => Parser[B]): Parser[B] = flatMap(_ => p2)

  override def or[B >: A](p2: => Parser[B]): Parser[B] = AbstractParser(
    s => exec(s) match {
      case Failure(e,false) => p2.exec(s)
      case r => r
    }
  )

  override def skipL[B](p2: => Parser[B]): Parser[B] = slice.map2(p2)((a,b) => b)

  override def skipR(p2: => Parser[_]): Parser[A] = map2(p2.slice)((a,b) => a)

  override def opt: Parser[Option[A]] = //map(Some(_)) or AbstractParsers.succeed(None)
    AbstractParser(
      s => exec(s) match {
        case Success(get, length) => Success(Some(get),length)
        case Failure(_, _) => Success(None,0)
      }
    )

  override def sep(p2: Parser[_]): Parser[List[A]] = sep1(p2) or AbstractParsers.succeed(List())

  override def sep1(p2: Parser[_]): Parser[List[A]] = map2(AbstractParsers.many(p2 *> this))(_ :: _)

  override def listOfN(n: Int): Parser[List[A]] =
    if (n <= 0) AbstractParsers.succeed(List())
    else map2(listOfN(n-1))(_ :: _)
}
