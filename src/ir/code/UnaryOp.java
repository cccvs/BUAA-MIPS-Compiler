package ir.code;

import ir.operand.Operand;

public class UnaryOp implements BasicIns{
    // Originate from UnaryExp
    public enum Type {
        MOV,
        NEG,
        NOT     // logic not
    }

    private Type op;
    private Operand src;
    private Operand dst;

    public UnaryOp(Type op, Operand src, Operand dst) {
        this.op = op;
        this.src = src;
        this.dst = dst;
    }

    @Override
    public String toString() {
        return "\t" + op.name() + " " + dst + ", " + src;
    }
}
