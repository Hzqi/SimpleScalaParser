package com.jackywong.parsing;

import com.jackywong.safer.Tuple;
import com.jackywong.scala.parsing.my.ParseResult;
import com.jackywong.scala.parsing.my.ParseState;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by huangziqi on 2019/7/16
 */
public interface Parser<A> {
    ParseResult<? extends A> exec(ParseState state);

    Parser<? extends A> label(String msg);
    Parser<? extends A> scope(String msg);
    Parser<? extends A> attempt();
    Parser<String> slice();

    <B> Parser<B> flatMap(Function<A,Parser<B>> f);
    <B> Parser<B> map(Function<A,B> f);
    <B,C> Parser<C> map2(Supplier<Parser<B>> p2, Function<Tuple<A,B>,C> f);
    <B> Parser<Tuple<A,B>> product(Supplier<Parser<B>> p2);
    <B> Parser<B> as(B b);

    Parser<? super A> and(Supplier<Parser<?>> p2);
    Parser<? super A> or(Supplier<Parser<?>> p2);
    <B> Parser<B> skipL(Supplier<Parser<B>> p2);
    Parser<A> skipR(Supplier<Parser<?>> p2);
    Parser<Optional<A>> opt();
    Parser<List<A>> sep(Parser<?> p2);
    Parser<List<A>> sep1(Parser<?> p2);
    Parser<List<A>> listOfN(int n);
}
