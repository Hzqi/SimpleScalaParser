package com.jackywong.safer;

import java.util.function.Function;

/**
 * Created by huangziqi on 2019/7/1
 */
public final class Left<L,R> implements Either<L,R> {
    private L value;

    public Left(L value) {
        this.value = value;
    }

    @Override
    public boolean isLeft() {
        return true;
    }

    @Override
    public L getLeft() {
        return value;
    }

    @Override
    public <U> Either<L, U> mapRight(Function<R, U> f) {
        return new Left<>(value);
    }

    @Override
    public <U> Either<U, R> mapLeft(Function<L, U> f) {
        return new Left<>(f.apply(value));
    }
}
