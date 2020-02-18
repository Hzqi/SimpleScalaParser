package com.jackywong.parsing.arithmetic;

import com.jackywong.safer.Tuple;
import static com.jackywong.parsing.arithmetic.Parsers.*;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by huangziqi on 2020/2/18
 */
@FunctionalInterface
public interface Parser<A> extends Function<String, Optional<Tuple<A,String>>> {
    default Parser<A> self() {
        return this;
    }

    default <B> Parser<B> bind(Function<A,Parser<B>> f) {
        return (String s) -> {
            Optional<Tuple<A,String>> opt = self().apply(s);
            if(opt.isPresent()) {
                Parser<B> b = f.apply(opt.get().get_1());
                return b.apply(opt.get().get_2());
            } else {
                return Optional.empty();
            }
        };
    }

    default <B> Parser<B> skipLeft(Supplier<Parser<B>> parser) {
        return self().bind(a -> parser.get());
    }

    default <B> Parser<A> skipRight(Supplier<Parser<B>> parser) {
        return self().bind(a -> parser.get().bind(b -> just(a)));
    }

    default <B> Parser<B> map(Function<A,B> f) {
        return (String s) -> {
            Optional<Tuple<A,String>> opt = self().apply(s);
            if(opt.isPresent()) {
                Tuple<B,String> tuple = Tuple.of(f.apply(opt.get().get_1()),opt.get().get_2());
                return Optional.of(tuple);
            } else {
                return Optional.empty();
            }
        };
    }

    default Parser<A> or(Supplier<Parser<A>> parser) {
        return (String s) -> {
            Optional<Tuple<A,String>> opt = self().apply(s);
            if(!opt.isPresent()) {
                return parser.get().apply(s);
            } else {
                return opt;
            }
        };
    }
}
