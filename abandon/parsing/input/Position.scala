package com.jackywong.scala.parsing.input

/**
  * Created by huangziqi on 2019/6/26
  */
trait Position {
  def line: Int

  def column: Int

  protected def lineContents: String

  override def toString = s"$line.$column"

  def longString =
    lineContents + "\n" +
      lineContents
        .take(column-1)
        .map{x =>
          if (x == '\t') x
          else ' '
        } + "^"

  def <(that: Position) = {
    this.line < that.line ||
      this.line == that.line && this.column < that.column
  }
}
