package com.jackywong.scala.parsing.combinator

import com.jackywong.scala.parsing.input.{NoPosition, Positional, Reader}
import com.jackywong.scala.parsing.result._

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

/**
  * Created by huangziqi on 2019/6/26
  */
abstract class Parser[+T,Elem] extends (Reader[Elem] => ParseResult[T,Elem]) {
  type Input = Reader[Elem]

  private var name: String = ""
  def named(n: String): this.type = {name=n; this}
  override def toString = s"Parser ($name)"

  def apply(in: Input): ParseResult[T,Elem]

  def flatMap[U](f: T => Parser[U,Elem]): Parser[U,Elem] = Parser.makeParser { in =>
    apply(in).flatMapWithNext(f)
  }

  def map[U](f: T => U): Parser[U,Elem] = Parser.makeParser{ in => apply(in) map(f)}

  def filter(p: T => Boolean): Parser[T,Elem] = withFilter(p)

  def withFilter(p: T => Boolean): Parser[T,Elem] = Parser.makeParser{ in =>
    apply(in)
      .filterWithError(p, "Input doesn't match filter: "+_, in)}

  def append[U >: T](p0: => Parser[U,Elem]): Parser[U,Elem] = {
    lazy val p = p0 // lazy argument
    Parser.makeParser{ in =>
      apply(in).append(p(in)) //p只要没有调用apply就没有执行
    }
  }

  def ~ [U](q: => Parser[U,Elem]): Parser[~[T, U],Elem] = {
    lazy val p = q // lazy argument
    (
      for(a <- this; b <- p)
        yield new ~(a,b)
      ).named("~")
  }

  def ~> [U](q: => Parser[U,Elem]): Parser[U,Elem] = {
    lazy val p = q // lazy argument
    (for(a <- this; b <- p) yield b).named("~>")
  }

  def <~ [U](q: => Parser[U,Elem]): Parser[T,Elem] = {
    lazy val p = q // lazy argument
    (for(a <- this; b <- p) yield a).named("<~")
  }

  def - [U](q: Parser[U,Elem]): Parser[T,Elem] = (Parser.not(q) ~> this).named("-")

  def ~! [U](p: => Parser[U,Elem]): Parser[~[T, U],Elem]
  = OnceParser.makeOnceParser{ (for(a <- this; b <- Parser.commit(p)) yield new ~(a,b)).named("~!") }

  def ~>! [U](q: => Parser[U,Elem]): Parser[U,Elem] = { lazy val p = q // lazy argument
    OnceParser.makeOnceParser { (for(a <- this; b <- Parser.commit(p)) yield b).named("~>!") }
  }

  def <~! [U](q: => Parser[U,Elem]): Parser[T,Elem] = { lazy val p = q // lazy argument
    OnceParser.makeOnceParser { (for(a <- this; b <- Parser.commit(p)) yield a).named("<~!") }
  }

  def | [U >: T](q: => Parser[U,Elem]): Parser[U,Elem] = append(q).named("|")

  def ||| [U >: T](q0: => Parser[U,Elem]): Parser[U,Elem] = new Parser[U,Elem] {
    lazy val q = q0 // lazy argument
    def apply(in: Input) = {
      val res1 = Parser.this(in)
      val res2 = q(in)

      (res1, res2) match {
        case (s1 @ Success(_, next1), s2 @ Success(_, next2)) => if (next2.pos < next1.pos || next2.pos == next1.pos) s1 else s2
        case (s1 @ Success(_, _), _) => s1
        case (_, s2 @ Success(_, _)) => s2
        case (e1 @ Error(_, _), _) => e1
        case (f1 @ Failure(_, next1), ns2 @ NoSuccess(_, next2)) => if (next2.pos < next1.pos || next2.pos == next1.pos) f1 else ns2
      }
    }
    override def toString = "|||"
  }

  def ^^ [U](f: T => U): Parser[U,Elem] = map(f).named(toString+"^^")

  def ^^^ [U](v: => U): Parser[U,Elem] =  new Parser[U,Elem] {
    lazy val v0 = v // lazy argument
    def apply(in: Input) = Parser.this(in) map (x => v0)
  }.named(toString+"^^^")

  def ^? [U](f: PartialFunction[T, U], error: T => String): Parser[U,Elem] = Parser.makeParser{ in:Input =>
    this(in).mapPartial(f, error)}.named(toString+"^?")

  def ^? [U](f: PartialFunction[T, U]): Parser[U,Elem] = ^?(f, r => "Constructor function not defined at "+r)

  def into[U](fq: T => Parser[U,Elem]): Parser[U,Elem] = flatMap(fq)

  def >>[U](fq: T => Parser[U,Elem])=into(fq)

  def * = Parser.rep(this)

  def *[U >: T](sep: => Parser[(U, U) => U,Elem]) = Parser.chainl1(this, sep)

  def + = Parser.rep1(this)

  def ? = Parser.opt(this)

  def withFailureMessage(msg: String) = Parser.makeParser{ in: Input =>
    apply(in) match {
      case Failure(_, next) => Failure(msg, next)
      case other            => other
    }
  }

  def withErrorMessage(msg: String) = Parser.makeParser{ in: Input =>
    this(in) match {
      case Error(_, next) => Error(msg, next)
      case other          => other
    }
  }
}





object Parser {
  def makeParser[T,Elem](f: Reader[Elem] => ParseResult[T,Elem]): Parser[T,Elem] = new Parser[T,Elem] {
    override def apply(in: Input): ParseResult[T, Elem] = f(in)
  }

  def commit[T,Elem](p: => Parser[T,Elem]): Parser[T, Elem] = makeParser{ in =>
    p(in) match{
      case s @ Success(_, _) => s
      case e @ Error(_, _) => e
      case f @ Failure(msg, next) => Error(msg, next)
    }
  }

  def elem[Elem](kind: String, p: Elem => Boolean): Parser[Elem, Elem] = acceptIf(p)(inEl => kind+" expected")

  def elem[Elem](e: Elem): Parser[Elem,Elem] = accept(e)

  implicit def accept[Elem](e: Elem): Parser[Elem,Elem] = acceptIf[Elem](_ == e)("'"+e+"' expected but " + _ + " found")

  def accept[ES,Elem](es: ES)(implicit f: ES => List[Elem]): Parser[List[Elem],Elem] = acceptSeq(es)

  def accept[U,Elem](expected: String, f: PartialFunction[Elem, U]): Parser[U,Elem] = acceptMatch(expected, f)

  def acceptIf[Elem](p: Elem => Boolean)(err: Elem => String): Parser[Elem,Elem] = makeParser { in =>
    if (in.atEnd) Failure("end of input", in)
    else if (p(in.first)) Success(in.first, in.rest)
    else Failure(err(in.first), in)
  }

  def acceptMatch[U,Elem](expected: String, f: PartialFunction[Elem, U]): Parser[U,Elem] = makeParser { in =>
    if (in.atEnd) Failure("end of input", in)
    else if (f.isDefinedAt(in.first)) Success(f(in.first), in.rest)
    else Failure(expected+" expected", in)
  }

  def acceptSeq[ES,Elem](es: ES)(implicit f: ES => Iterable[Elem]): Parser[List[Elem],Elem] =
    es.foldRight[Parser[List[Elem],Elem]] (success(Nil)) {
      (x, pxs) => accept[Elem](x) ~ pxs ^^ mkList
    }

  def failure[Elem](msg: String) = makeParser{ in => Failure[Elem](msg, in) }

  def err[Elem](msg: String) = makeParser{ in => Error[Elem](msg, in) }

  def success[T,Elem](v: T) = makeParser{ in => Success[T,Elem](v, in) }

  def log[T,Elem](p: => Parser[T,Elem])(name: String): Parser[T,Elem] = makeParser{ in =>
    println("trying "+ name +" at "+ in)
    val r = p(in)
    println(name +" --> "+ r)
    r
  }


  def rep[T,Elem](p: => Parser[T,Elem]): Parser[List[T],Elem] = rep1(p) | success(List())


  def repsep[T,Elem](p: => Parser[T,Elem], q: => Parser[Any,Elem]): Parser[List[T],Elem] =
    rep1sep(p, q) | success(List())


  def rep1[T,Elem](p: => Parser[T,Elem]): Parser[List[T],Elem] = rep1(p, p)


  def rep1[T, Elem](first: => Parser[T, Elem], p0: => Parser[T, Elem]): Parser[List[T], Elem] = makeParser { in =>
    lazy val p = p0 // lazy argument
    val elems = new ListBuffer[T]

    def continue(in: Reader[Elem]): ParseResult[List[T], Elem] = {
      val p0 = p // avoid repeatedly re-evaluating by-name parser

      @tailrec
      def applyp(in0: Reader[Elem]): ParseResult[List[T], Elem] = p0(in0) match {
        case Success(x, rest) => elems += x; applyp(rest)
        case e@Error(_, _) => e // still have to propagate error
        case _ => Success(elems.toList, in0)
      }
      applyp(in)
    }

    first(in) match {
      case Success(x, rest) => elems += x; continue(rest)
      case ns: NoSuccess[Elem] => ns
    }
  }

  def repN[T, Elem](num: Int, p: => Parser[T, Elem]): Parser[List[T], Elem] =
    if (num == 0) success(Nil)
    else makeParser { in =>
      val elems = new ListBuffer[T]
      val p0 = p    // avoid repeatedly re-evaluating by-name parser

      @tailrec
      def applyp(in0: Reader[Elem]): ParseResult[List[T], Elem] =
        if (elems.length == num) Success(elems.toList, in0)
        else p0(in0) match {
          case Success(x, rest) => elems += x ; applyp(rest)
          case ns: NoSuccess[Elem]    => ns
        }

      applyp(in)
    }

  def rep1sep[T,Elem](p : => Parser[T,Elem], q : => Parser[Any,Elem]): Parser[List[T],Elem] =
    p ~ rep(q ~> p) ^^ {case x~y => x :: y }

  def chainl1[T,Elem](p: => Parser[T,Elem], q: => Parser[(T, T) => T,Elem]): Parser[T,Elem]
  = chainl1(p, p, q)

  def chainl1[T, U,Elem](first: => Parser[T,Elem], p: => Parser[U,Elem], q: => Parser[(T, U) => T,Elem]): Parser[T,Elem]
  = first ~ rep(q ~ p) ^^ {
    case x ~ xs => xs.foldLeft(x: T){case (a, f ~ b) => f(a, b)} // x's type annotation is needed to deal with changed type inference due to SI-5189
  }

  def chainr1[T, U,Elem](p: => Parser[T,Elem], q: => Parser[(T, U) => U,Elem], combine: (T, U) => U, first: U): Parser[U,Elem]
  = p ~ rep(q ~ p) ^^ {
    case x ~ xs => (new ~(combine, x) :: xs).foldRight(first){case (f ~ a, b) => f(a, b)}
  }

  def opt[T,Elem](p: => Parser[T,Elem]): Parser[Option[T],Elem] =
    p ^^ (x => Some(x)) | success(None)


  def not[T,Elem](p: => Parser[T,Elem]): Parser[Unit,Elem] = makeParser { in =>
    p(in) match {
      case Success(_, _)  => Failure("Expected failure", in)
      case _              => Success((), in)
    }
  }

  def guard[T,Elem](p: => Parser[T,Elem]): Parser[T,Elem] = makeParser { in =>
    p(in) match{
      case s@ Success(s1,_) => Success(s1, in)
      case e => e
    }
  }

  def positioned[T <: Positional,Elem](p: => Parser[T,Elem]): Parser[T,Elem] = makeParser { in =>
    p(in) match {
      case Success(t, in1) => Success(if (t.pos == NoPosition) t setPos in.pos else t, in1)
      case ns: NoSuccess[Elem] => ns
    }
  }

  def phrase[T,Elem](p: Parser[T,Elem]) = new Parser[T,Elem] {
    def apply(in: Input) = p(in) match {
      case s @ Success(out, in1) =>
        if (in1.atEnd) s
        else Failure("end of input expected", in1)
      case ns => ns
    }
  }

  def mkList[T] = (_: ~[T, List[T]]) match { case x ~ xs =>  x :: xs }
}