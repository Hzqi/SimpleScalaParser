package com.jackywong.scala.parsing.result

import com.jackywong.scala.parsing.input.Reader

/**
  * Created by huangziqi on 2019/6/26
  */
abstract class ParseResult[+T,Elem] {
  type Input = Reader[Elem]

  def map[U](f: T => U): ParseResult[U,Elem]

  def mapPartial[U](f: PartialFunction[T, U], error: T => String): ParseResult[U,Elem]

  def flatMapWithNext[U](f: T => Input => ParseResult[U,Elem]): ParseResult[U,Elem]

  def filterWithError(p: T => Boolean, error: T => String, position: Input): ParseResult[T,Elem]

  def append[U >: T](a: => ParseResult[U,Elem]): ParseResult[U,Elem]

  def isEmpty = !successful

  def get: T

  def getOrElse[B >: T](default: => B): B =
    if (isEmpty) default else this.get

  val next: Input

  val successful: Boolean
}
