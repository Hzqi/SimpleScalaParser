package com.jackywong.scala.parsing.result

import com.jackywong.scala.parsing.input.Reader

/**
  * Created by huangziqi on 2019/6/26
  */
abstract class NoSuccess[Elem](val msg: String, override val next: Reader[Elem]) extends ParseResult[Nothing,Elem] { // when we don't care about the difference between Failure and Error
  val successful = false

  def map[U](f: Nothing => U) = this
  def mapPartial[U](f: PartialFunction[Nothing, U], error: Nothing => String): ParseResult[U,Elem] = this

  def flatMapWithNext[U](f: Nothing => Input => ParseResult[U,Elem]): ParseResult[U,Elem]
  = this

  def filterWithError(p: Nothing => Boolean, error: Nothing => String, position: Input): ParseResult[Nothing,Elem] = this

  def get: Nothing = scala.sys.error("No result when parsing failed")
}
/** An extractor so `NoSuccess(msg, next)` can be used in matches. */
object NoSuccess {
  def unapply[T,Elem](x: ParseResult[T,Elem]) = x match {
    case Failure(msg, next)   => Some((msg, next))
    case Error(msg, next)     => Some((msg, next))
    case _                    => None
  }
}
