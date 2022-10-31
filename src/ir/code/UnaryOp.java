package ir.code;

import ir.operand.Operand;
import ir.operand.TmpVar;

public class UnaryOp implements BasicIns{
    // Originate from UnaryExp
    public enum Type {
        NEG,
        NOT     // logic not
    }

    private Type op;
    private Operand src;
    private TmpVar dst;

    public UnaryOp(Type op, Operand src, TmpVar dst) {
        this.op = op;
        this.src = src;
        this.dst = dst;
    }
}
