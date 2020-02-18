package com.jackywong.parsing.arithmetic;

import com.jackywong.parsing.arithmetic.expr.Bin;
import com.jackywong.parsing.arithmetic.expr.Con;
import com.jackywong.parsing.arithmetic.expr.Expr;
import com.jackywong.parsing.arithmetic.expr.Op;
import com.jackywong.safer.Tuple;

import java.util.Optional;
import java.util.function.Supplier;

import static com.jackywong.parsing.arithmetic.Parsers.*;

/**
 * Created by huangziqi on 2020/2/18
 */
public class Arithmetic {

    public Parser<Expr> expr() {
        return token(() -> term().bind(e -> rest(e)));
    }

    public Parser<Expr> rest(Expr e1) {
        return addop().bind(op ->
                term().bind(e2 ->
                        rest(new Bin(op,e1,e2))
                )
        ).or(() -> just(e1));
    }

    public Parser<Expr> term() {
        return token(() -> factor().bind(e -> more(e)));
    }

    public Parser<Expr> more(Expr e1) {
        return mulop().bind(op ->
                factor().bind(e2 ->
                        more(new Bin(op,e1,e2))
                )
        ).or(() -> just(e1));
    }

    public Parser<Expr> factor() {
        return token(() -> constant().or(() -> paren(() -> expr())));
    }

    public Parser<Expr> constant() {
        return pint().bind(n -> just(new Con(n)));
    }

    public Parser<Expr> paren(Supplier<Parser<Expr>> parser) {
        return symbol("(").skipLeft(() -> parser.get().skipRight(() -> symbol(")")));
    }

    public Parser<Op> addop() {
        return symbol("+").skipLeft(() -> just(Op.Plus))
                .or(() -> symbol("-").skipLeft(() -> just(Op.Minus)));
    }

    public Parser<Op> mulop() {
        return symbol("*").skipLeft(() -> just(Op.Mul))
                .or(() -> symbol("/").skipLeft(() -> just(Op.Div)));
    }

    public static void main(String[] args) {
        Arithmetic arithmetic = new Arithmetic();
        Optional<Tuple<Expr,String>> tuple = arithmetic.expr().apply("1+2*3/4");
        System.out.println(tuple);
        System.out.println("result:" + tuple.get().get_1().calc());
    }
}
