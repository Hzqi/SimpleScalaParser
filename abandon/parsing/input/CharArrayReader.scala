package com.jackywong.scala.parsing.input

/**
  * Created by huangziqi on 2019/6/27
  */
object CharArrayReader {
  final val EofCh = '\u001a'
}

/** A character array reader reads a stream of characters (keeping track of their positions)
  * from an array.
  *
  * @param chars  an array of characters
  * @param index  starting offset into the array; the first element returned will be `source(index)`
  *
  * @author Martin Odersky
  * @author Adriaan Moors
  */
class CharArrayReader(chars: Array[Char], index: Int) extends CharSequenceReader(chars, index) {

  def this(chars: Array[Char]) = this(chars, 0)

}
