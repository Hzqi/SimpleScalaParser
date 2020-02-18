package com.jackywong.parsing.arithmetic.expr;

/**
 * Created by huangziqi on 2020/2/18
 */
public class Con implements Expr {
    private Integer n;

    public Con(Integer n) {
        this.n = n;
    }

    public Integer getN() {
        return n;
    }

    public void setN(Integer n) {
        this.n = n;
    }

    @Override
    public String toString() {
        return "Con(" +
                "" + n +
                ')';
    }

    @Override
    public Double calc() {
        return this.n.doubleValue();
    }
}
