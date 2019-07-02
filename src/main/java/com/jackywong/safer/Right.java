package com.jackywong.safer;

import java.util.function.Function;

/**
 * Created by huangziqi on 2019/7/1
 */
public final class Right<L,R> implements Either<L,R> {
    private R value;

    public Right(R value) {
        this.value = value;
    }

    @Override
    public boolean isRight() {
        return true;
    }

    @Override
    public R getRight() {
        return value;
    }

    @Override
    public <U> Either<L, U> mapRight(Function<R, U> f) {
        return new Right<>(f.apply(value));
    }

    @Override
    public <U> Either<U, R> mapLeft(Function<L, U> f) {
        return new Right<>(value);
    }
}
