package com.jackywong.safer.match;

import com.jackywong.safer.Tuple;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by huangziqi on 2019/7/16
 */
public class MatchCases<A> {
    private Object obj;
    private LinkedList<Tuple<Class,Function>> cases;
    private Supplier<A> matchDefault;

    <B> MatchCases(Object obj, Class<B> clazz, Function<B, A> func){
        this.cases = new LinkedList<>();
        this.cases.add(new Tuple<>(clazz,func));
        this.obj = obj;
    }

    MatchCases(Object obj, Supplier<A> func) {
        this.obj = obj;
        this.matchDefault = func;
    }

    public <B> MatchCases<A> matchCases(Class<B> clazz, Function<B,A> func) {
        this.cases.add(new Tuple<>(clazz,func));
        return this;
    }

    public MatchCases<A> matchDefault(Supplier<A> func) {
        this.matchDefault = func;
        return this;
    }

    public MatcherFinished<A> build() {
        return new MatcherFinished<>(obj,cases,matchDefault);
    }
}
