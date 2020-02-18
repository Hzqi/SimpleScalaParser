package com.jackywong.safer;

/**
 * Created by huangziqi on 2019/7/2
 */
public class Tuple<A,B> {
    private A _1;
    private B _2;

    public static <A,B> Tuple<A,B> of(A a,B b) {
        return new Tuple<>(a,b);
    }

    public Tuple(A _1, B _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public A get_1() {
        return _1;
    }

    public B get_2() {
        return _2;
    }

    @Override
    public String toString() {
        return "Tuple{" +
                _1 +
                "," + _2 +
                '}';
    }
}
