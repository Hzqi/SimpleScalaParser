package com.jackywong.safer.match;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by huangziqi on 2019/7/16
 */
public class MatchBuilder<A> {
    private Object obj;

    MatchBuilder(Object obj) {
        this.obj = obj;
    }

    public <B> MatchCases<A> matchCases(Class<B> clazz, Function<B,A> func) {
        return new MatchCases<>(obj,clazz,func);
    }

    public MatchCases<A> matchCases(Supplier<A> func) {
        return new MatchCases<>(obj,func);
    }
}
