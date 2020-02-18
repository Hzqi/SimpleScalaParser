package com.jackywong.parsing.arithmetic;

import com.jackywong.safer.Tuple;
import static com.jackywong.parsing.arithmetic.Tools.*;

import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by huangziqi on 2020/2/18
 */
public class Parsers {
    public static <A> Parser<A> just(A a) {
        return (String s) -> Optional.of(Tuple.of(a,s));
    }

    public static Parser<Character> getc() {
        return (String s) -> {
            if (s.isEmpty()) {
                return Optional.empty();
            } else {
                Tuple<Character,String> tuple = Tuple.of(s.charAt(0),s.substring(1));
                return Optional.of(tuple);
            }
        };
    }

    public static <A> Parser<A> nil() {
        return (String s) -> Optional.empty();
    }

    public static Parser<Character> sat(Predicate<Character> f){
        return getc().bind(c -> {
            if (f.test(c))
                return just(c);
            else
                return nil();
        });
    }

    public static Parser<Character> pchar(Character s) {
        return sat(c -> c.equals(s));
    }

    public static Parser<String> pstring(String s) {
        if (s.isEmpty()) {
            return just("");
        } else {
            return pchar(s.charAt(0)).bind(c ->
                    pstring(s.substring(1)).bind(cs ->
                            just(c + cs)
                    )
            );
        }
    }

    public static Parser<Integer> digit() {
        return sat(Tools::isDigit).bind(c -> just(cvt(c)));
    }

    public static <A> Parser<LinkedList<A>> many1(Parser<A> parser) {
        return parser.bind(a ->
            many(parser).bind((LinkedList<A> as) -> {
                LinkedList<A> list = as;
                list.addFirst(a);
                return just(list);
            })
        );
    }

    public static <A> Parser<LinkedList<A>> many(Parser<A> parser) {
        return many1(parser).or(() -> just(new LinkedList<>()));
    }

    public static Parser<String> space() {
        return many(sat(Tools::isSpace)).skipLeft(() -> just(""));
    }

    public static Parser<String> symbol(String xs){
        return space().skipLeft(() -> pstring(xs));
    }

    public static <A> Parser<A> token(Supplier<Parser<A>> parser) {
        return space().skipLeft(() -> parser.get().skipRight(() -> space()));
    }

    public static <A> Parser<LinkedList<A>> optional(Supplier<Parser<LinkedList<A>>> parser) {
        return parser.get().or(() -> just(new LinkedList<>()));
    }

    public static <A,B> Parser<LinkedList<A>> manyWith1(Supplier<Parser<B>> sep, Supplier<Parser<A>> parser) {
        return parser.get().bind(x ->
                many(sep.get().skipLeft(parser)).bind(xs ->{
                    LinkedList<A> list = xs;
                    list.addFirst(x);
                    return just(list);
                })
        );
    }

    public static <A,B> Parser<LinkedList<A>> manyWith(Supplier<Parser<B>> sep, Supplier<Parser<A>> parser) {
        return optional(() -> manyWith1(sep,parser));
    }

    public static Parser<Integer> nat() {
        return many1(digit()).bind(ds -> {
            Optional<Integer> opt = ds.stream().reduce(Tools::shiftl);
            return opt.map(Parsers::just).orElseGet(() -> just(0));
        });
    }

    public static Parser<Integer> natural() {
        return token(() -> nat());
    }

    public static Parser<Integer> pint() {
        return symbol("-").skipLeft(() -> nat())
                .bind(n -> just(-n))
                .or(() -> natural());
    }
}
