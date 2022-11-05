package ir.code;

import ir.operand.Operand;
import ir.operand.MidVar;

public class BinaryOp implements BasicIns{
    public enum Type {
        ADD, SUB, MUL, DIV, MOD, SGE, SGT, SLE, SLT, SEQ, SNE, AND, OR
    }


    // Originate from BinaryExp
    private Type op;
    private Operand src1;
    private Operand src2;
    private MidVar dst;

    public BinaryOp(Type op, Operand src1, Operand src2, MidVar dst) {
        this.op = op;
        this.src1 = src1;
        this.src2 = src2;
        this.dst = dst;
    }

    @Override
    public String toString() {
        return "\t" + op.name() + " " + dst + ", " + src1 + ", " + src2;
    }

    public Type getOp() {
        return op;
    }

    public Operand getSrc1() {
        return src1;
    }

    public Operand getSrc2() {
        return src2;
    }

    public MidVar getDst() {
        return dst;
    }
}
