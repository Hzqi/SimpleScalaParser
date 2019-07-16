package com.jackywong.safer.match;

import java.util.function.Function;

/**
 * Created by huangziqi on 2019/7/16
 */
public class Matcher {

    public static <A> MatchBuilder<A> match(Object obj){
        return new MatchBuilder<>(obj);
    }

    public static <A,B> MatchCasesLack matchCases(Class<B> clazz, Function<B,A> func) {
        return new MatchCasesLack<>(clazz,func);
    }
}
