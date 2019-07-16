package com.jackywong.parsing;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Created by huangziqi on 2019/7/16
 */
public interface Parsers {
    <A> Parser<A> defaultSucceed(A a);
    <A> Parser<A> succeed(A a);
    Parser<String> string(String s);
    Parser<Character> pchar(Character c);
    Parser<Character> charIn(Character... c);
    Parser<Character> charNotIn(Character... c);
    <A> Parser<List<A>> many1(Parser<A> p);
    <A> Parser<? super List<A>> many(Parser<A> p);
    Parser<String> regex(String r);
    Parser<String> whitespace();
    Parser<String> digits();
    Parser<String> thru(String s);
    Parser<String> quoted();
    Parser<String> escaped();
    Parser<String> escapedQuoted();
    Parser<String> doubleString();
    Parser<Double> pdouble();
    <A> Parser<A> token(Parser<A> p);
    <A> Parser<A> opL(Parser<A> p, Parser<BiFunction<A,A,A>> op);
    <A> Parser<A> surround(Parser<?> start, Parser<?> stop, Supplier<Parser<A>> p);
    Parser<String> eof();
    <A> Parser<A> root(Parser<A> p);
}
