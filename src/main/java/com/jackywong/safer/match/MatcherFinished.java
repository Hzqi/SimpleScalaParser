package com.jackywong.safer.match;

import com.jackywong.safer.Tuple;

import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by huangziqi on 2019/7/16
 */
public class MatcherFinished<A> {
    private LinkedList<Tuple<Class,Function>> func;
    private Object obj;
    private Supplier<A> matchDefault;

    MatcherFinished(Object obj, LinkedList<Tuple<Class, Function>> func, Supplier<A> matchDefault) {
        this.func = func;
        this.obj = obj;
        this.matchDefault = matchDefault;
    }

    public A get() {
        for (Tuple<Class,Function> tuple : func) {
            Class clazz = tuple.get_1();
            if(clazz.isInstance(obj)) {
                return (A) tuple.get_2().apply(clazz.cast(obj));
            }
        }
        return matchDefault.get();
    }
}
