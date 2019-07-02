package com.jackywong.safer;

import java.util.function.Function;

/**
 * Created by huangziqi on 2019/7/1
 */
public interface Either<L,R> {
    default boolean isLeft(){
        return false;
    }
    default boolean isRight(){
        return false;
    }

    default L getLeft() {
        throw new RuntimeException("this is not a Left");
    }
    default R getRight() {
        throw new RuntimeException("this is not a Right");
    }

    <U> Either<L,U> mapRight(Function<R,U> f);
    <U> Either<U,R> mapLeft(Function<L,U> f);
}
