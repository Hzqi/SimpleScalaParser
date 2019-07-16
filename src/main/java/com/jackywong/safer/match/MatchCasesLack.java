package com.jackywong.safer.match;

import com.jackywong.safer.Tuple;

import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by huangziqi on 2019/7/16
 */
public class MatchCasesLack<A> {
    private LinkedList<Tuple<Class,Function>> cases;
    private Supplier<A> matchDefault;

    <B> MatchCasesLack(Class<B> clazz, Function<B,A> func) {
        this.cases = new LinkedList<>();
        this.cases.add(new Tuple<>(clazz,func));
    }

    MatchCasesLack(Supplier<A> func) {
        this.matchDefault = func;
    }

    public <B> MatchCasesLack<A> matchCases(Class<B> clazz, Function<B,A> func) {
        this.cases.add(new Tuple<>(clazz,func));
        return this;
    }

    public MatchCasesLack<A> matchDefault(Supplier<A> func) {
        this.matchDefault = func;
        return this;
    }

    public MatcherFinished<A> build(Object obj) {
        return new MatcherFinished<>(obj,cases,matchDefault);
    }
}
