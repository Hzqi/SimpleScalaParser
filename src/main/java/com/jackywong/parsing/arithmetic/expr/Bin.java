package com.jackywong.parsing.arithmetic.expr;

/**
 * Created by huangziqi on 2020/2/18
 */
public class Bin implements Expr {
    private Op op;
    private Expr e1;
    private Expr e2;

    public Bin(Op op, Expr e1, Expr e2) {
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;
    }

    public Op getOp() {
        return op;
    }

    public void setOp(Op op) {
        this.op = op;
    }

    public Expr getE1() {
        return e1;
    }

    public void setE1(Expr e1) {
        this.e1 = e1;
    }

    public Expr getE2() {
        return e2;
    }

    public void setE2(Expr e2) {
        this.e2 = e2;
    }

    @Override
    public String toString() {
        return "Bin(" +
                "" + op +
                ", " + e1 +
                ", " + e2 +
                ')';
    }

    @Override
    public Double calc() {
        Double r1 = this.e1.calc();
        Double r2 = this.e2.calc();
        switch (this.op) {
            case Plus: return r1 + r2;
            case Minus: return r1 - r2;
            case Mul: return r1 * r2;
            case Div: return r1 / r2;
            default: return 0d;
        }
    }
}
