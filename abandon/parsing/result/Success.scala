package com.jackywong.scala.parsing.result

import com.jackywong.scala.parsing.input.Reader

/**
  * Created by huangziqi on 2019/6/26
  */
case class Success[+T,Elem](result: T, override val next: Reader[Elem]) extends ParseResult[T,Elem] {
  def map[U](f: T => U) = Success(f(result), next)
  def mapPartial[U](f: PartialFunction[T, U], error: T => String): ParseResult[U,Elem]
  = if(f.isDefinedAt(result)) Success(f(result), next)
  else Failure(error(result), next)

  def flatMapWithNext[U](f: T => Input => ParseResult[U,Elem]): ParseResult[U,Elem]
  = f(result)(next)

  def filterWithError(p: T => Boolean, error: T => String, position: Input): ParseResult[T,Elem] =
    if (p(result)) this
    else Failure(error(result), position)

  def append[U >: T](a: => ParseResult[U,Elem]): ParseResult[U,Elem] = this

  def get: T = result

  /** The toString method of a Success. */
  override def toString = s"[${next.pos}] parsed: $result"

  val successful = true
}
