package com.jackywong.scala.parsing.my

/**
  * Created by huangziqi on 2019/7/1
  * 解析结果
  */
sealed trait ParseResult[+A] {
  def extract: Either[ParseError,A] = this match {
    case Failure(e,_) => Left(e)
    case Success(a,_) => Right(a)
  }
  /* Used by `attempt`. */
  def uncommit: ParseResult[A] = this match {
    case Failure(e,true) => Failure(e,false)
    case _ => this
  }
  /* Used by `flatMap` */
  def addCommit(isCommitted: Boolean): ParseResult[A] = this match {
    case Failure(e,c) => Failure(e, c || isCommitted)
    case _ => this
  }
  /* Used by `scope`, `label`. */
  def mapError(f: ParseError => ParseError): ParseResult[A] = this match {
    case Failure(e,c) => Failure(f(e),c)
    case _ => this
  }
  def advanceSuccess(n: Int): ParseResult[A] = this match {
    case Success(a,m) => Success(a,n+m)
    case _ => this
  }
}
case class Success[+A](get: A, length: Int) extends ParseResult[A]
case class Failure(get: ParseError, isCommitted: Boolean) extends ParseResult[Nothing]