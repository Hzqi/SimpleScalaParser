package com.jackywong.safer;

import java.util.function.Supplier;

/**
 * Created by huangziqi on 2019/7/2
 */
public final class Lazy<T> {
    private T value;
    private Supplier<T> func;

    public Lazy(Supplier<T> func) {
        this.func = func;
    }

    public synchronized T get() {
        if(value == null) {
            value = func.get();
        }
        return value;
    }
}
