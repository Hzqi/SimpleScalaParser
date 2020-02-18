package com.jackywong.scala.parsing.arithmetic

/**
  * Created by huangziqi on 2020/2/18
  */
object Arithmetic {
  //静态工具
  case object Tools {
    private val digits = "0123456789".toCharArray
    def isDigit(c:Char):Boolean = digits.contains(c)
    def isSpace(c:Char):Boolean = c == ' '
    def cvt(c:Char):Int = c.toInt - '0'.toInt
    def shiftl(m:Int,n:Int):Int = 10 * m + n
  }

  //解析器type类型
  type _Parser[A] = String => Option[(A,String)]

  //解析器接口
  trait Parser[A] extends _Parser[A] {self =>
    //仿Haskell，其实就是flatMap的别名，用于连接（bind，组合）
    def >>=[B](f: A => Parser[B]):Parser[B] = (s: String) =>
      self.apply(s) match {
        case Some((x, s1)) => f(x)(s1)
        case None => None
      }
    //无传递连接，简单点理解就是：p >> q, 有状态相关的情况下，现在执行p，然后状态改变，然后执行q，状态再顺着改变
    def >>[B](parser: => Parser[B]):Parser[B] = self >>= (_ => parser)
    //无状态逆连接，其实和上面的相同，只是方向相反
    def <<[B](parser: => Parser[B]):Parser[A] = self >>= (a => parser >>= (_ => just(a)))
    //包装函数自动拆解，这个比较难解释，后面没用到，先无视
    def <*>[B](f: Parser[A => B]):Parser[B] = (s:String) =>
      f.apply(s) match {
        case Some((f1,s1)) => self.apply(s1) match {
          case Some((y,s2)) =>
            val v = f1(y)
            Some(v,s2)
          case None => None
        }
        case None => None
      }
    //map，很容易理解，就是映射，实际作用于执行后的转换结果类型工作
    def map[B](f:A => B):Parser[B] = (s:String) =>
      self.apply(s) match {
        case Some((x,s1)) => Some((f(x),s1))
        case None => None
      }
    //或逻辑
    def <|>(parser: => Parser[A]):Parser[A] = (s:String) => {
      val r = self(s)
      r match {
        case None => parser(s)
        case _ => r
      }
    }
  }

  def just[A](a: A):Parser[A] = (s: String) => Some(a, s)

  //基本的解析器，parser combinator基本解析器
  //基本读字符，只是读，没有判断功能
  def getc : Parser[Char] = (s:String) => if (s.isEmpty) None else Some((s.head,s.tail))
  //直接返回失败的结果，啥都没有
  def nil[A] : Parser[A] = (s:String) => None
  //基本读字符，带上字符判断的
  def sat(f:Char => Boolean) : Parser[Char] = getc >>= { c:Char =>
    if(f(c)) just(c) else nil
  }
  //标准读字符，仅读入参的字符
  def char(x:Char) = sat(_ == x)
  //标准读字符串，仅读入参的字符串
  def string(s:String):Parser[String] = if (s.isEmpty) just("") else {
    char(s.head) >>= {c:Char =>
      string(s.tail) >>= { cs: String =>
        just(s"${c}${cs}")
      }
    }
  }
  //标准读数字，单个字符
  def digit:Parser[Int] = sat(Tools.isDigit) >>= ((c:Char) => just(Tools.cvt(c)))


  //重复至少1次， 正则中的{}+
  def many1[A](parser: => Parser[A]):Parser[List[A]] = parser >>= { a: A =>
    many(parser) >>= { as: List[A] =>
      just(a :: as)
    }
  }
  //重复0次或多次，正则中的{}*
  def many[A](parser: => Parser[A]):Parser[List[A]] = many1(parser) <|> just(List.empty)
  //读连续的空格
  def space:Parser[_] = many(sat(Tools.isSpace)) >> just()
  //读终结符，前面带空格
  def symbol(xs:String):Parser[String] = space >> string(xs)
  //读token，对容易的解析器前后去空格
  def token[A](parser: => Parser[A]):Parser[A] = space >> parser << space
  //或读，有或者没有
  def optional[A](parser: => Parser[List[A]]):Parser[List[A]] = parser <|> just(List.empty)
  //带分隔解析的重复解析，比如是 1,2,3,4,5 这种，先读数字，再读逗号，逗号就是分隔符，这里是重复1次或多次
  def manyWith1[A,B](sep: => Parser[B],parser: => Parser[A]):Parser[List[A]] = parser >>= {x:A =>
    many(sep >> parser) >>= {xs:List[A] =>
      just(x::xs)
    }
  }
  //带分隔解析的重复解析，比如是 1,2,3,4,5 这种，先读数字，再读逗号，逗号就是分隔符，这里是重复0次或多次
  def manyWith[A,B](sep: => Parser[B],parser: => Parser[A]):Parser[List[A]] = optional(manyWith1(sep,parser))
  //不含空格的自然数分析器
  def nat:Parser[Int] = many1(digit) >>= {ds:List[Int] =>
    just(ds.foldLeft(0)(Tools.shiftl))
  }
  //自然数分析器(前后带空格)
  def natural:Parser[Int] = token(nat)
  //读整数，带负数的情况
  def int:Parser[Int] = ( symbol("-") >> nat >>= ((n:Int) => just(-n)) ) <|> natural


  //下面开始四则运算的解析和运算
  trait Expr { self => //算术表达式
  def calc : Double = self match {
    case Con(n) => n.toDouble
    case Bin(op,e1,e2) =>
      val a = e1.calc
      val b = e2.calc
      op match {
        case Plus => a+b
        case Minus => a-b
        case Mul => a*b
        case Div => a/b
      }
  }
  }
  trait Op   //算术操作符
  case class Con(n:Int) extends Expr      //单个数据
  case class Bin(op:Op, e1:Expr, e2:Expr) extends Expr//二元运算树
  case object Plus extends Op
  case object Minus extends Op
  case object Mul extends Op
  case object Div extends Op

  /*
  四则运算的BNF表达式
  expr ::= term {addop term}*
  term ::= factor {mulop factor}*
  factor ::= nat | paren expr
  paren ::= '(' expr ')'
  addop ::= '+' | '-'
  mulop ::= '*' | '/'
   */

  //顶级解析器，先读term，然后读rest
  def expr:Parser[Expr] = token(term >>= rest)

  //rest就是先读addop的符号再继续读一遍term，如果没有加减符号，就直接返回这个表达式
  def rest(e1:Expr):Parser[Expr] = (addop >>= {op:Op =>
    term >>= {e2:Expr =>
      rest(Bin(op,e1,e2))
    }
  }) <|> just(e1)

  //term就是先读一遍factor，然后再读more
  def term:Parser[Expr] = token(factor >>= more)

  //more就是先读mulop的符号再继续读一遍factor，如果没有乘除，就直接返回这个表达式，
  //注意，因为是递归处理的，乘除的递归层级比加减的深所以会先处理乘除，递归深度：addop -> mulop -> paren(括号)
  def more(e1:Expr):Parser[Expr] = (mulop >>= {op:Op =>
    factor >>= {e2:Expr =>
      more(Bin(op,e1,e2))
    }
  }) <|> just(e1)

  //factor其实就是读到底部，要么是单独的数据，要么是括号，括号内的话就是重头再读一次，不断递归
  def factor:Parser[Expr] = token(constant <|> paren(expr))

  //constant就是遇到单独的数的，再下去就没有带运算符的，直接给返回这个数
  def constant:Parser[Expr] = int >>= ((n:Int) => just(Con(n)))

  //算符号内的，先读有括号，然后就重头再来一遍递归，再读有括号，
  // 因为再一次的递归层级里最深，所以会最优先处理括号内的解析
  def paren(parser: => Parser[Expr]):Parser[Expr] = symbol("(") >> parser << symbol(")")

  //读加减法的符号，要么是+、要么是-
  def addop:Parser[Op] = (symbol("+") >> just(Plus.asInstanceOf[Op])) <|>
    (symbol("-") >> just(Minus.asInstanceOf[Op]))

  //读乘除号，要么*，要么/
  def mulop:Parser[Op] = (symbol("*") >> just(Mul.asInstanceOf[Op])) <|>
    (symbol("/") >> just(Div.asInstanceOf[Op]))

  def main(args: Array[String]): Unit = {
    val p = expr("1+(-2)*3/4").map(_._1)
    println(p)
    println(s"resutl: ${p.map(_.calc)}")
  }
}