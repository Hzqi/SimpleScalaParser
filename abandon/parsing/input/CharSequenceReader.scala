package com.jackywong.scala.parsing.input

import scala.util.parsing.input.{CharSequenceReader, OffsetPosition, Position, Reader}

/**
  * Created by huangziqi on 2019/6/27
  */
object CharSequenceReader {
  final val EofCh = '\u001a'
}

/** A character array reader reads a stream of characters (keeping track of their positions)
  * from an array.
  *
  * @param source the source sequence
  * @param offset  starting offset.
  *
  * @author Martin Odersky
  */
class CharSequenceReader(override val source: java.lang.CharSequence,
                         override val offset: Int) extends Reader[Char] {
  import CharSequenceReader._

  /** Construct a `CharSequenceReader` with its first element at
    *  `source(0)` and position `(1,1)`.
    */
  def this(source: java.lang.CharSequence) = this(source, 0)

  /** Returns the first element of the reader, or EofCh if reader is at its end.
    */
  def first =
    if (offset < source.length) source.charAt(offset) else EofCh

  /** Returns a CharSequenceReader consisting of all elements except the first.
    *
    * @return If `atEnd` is `true`, the result will be `this`;
    *         otherwise, it's a `CharSequenceReader` containing the rest of input.
    */
  def rest: CharSequenceReader =
    if (offset < source.length) new CharSequenceReader(source, offset + 1)
    else this

  /** The position of the first element in the reader.
    */
  def pos: Position = new OffsetPosition(source, offset)

  /** true iff there are no more elements in this reader (except for trailing
    *  EofCh's)
    */
  def atEnd = offset >= source.length

  /** Returns an abstract reader consisting of all elements except the first
    *  `n` elements.
    */
  override def drop(n: Int): CharSequenceReader =
    new CharSequenceReader(source, offset + n)

  /** Returns a String in the form `CharSequenceReader(first, ...)`,
    *  or `CharSequenceReader()` if this is `atEnd`.
    */
  override def toString: String = {
    val c = if (atEnd) "" else s"'$first', ..."
    s"CharSequenceReader($c)"
  }
}
