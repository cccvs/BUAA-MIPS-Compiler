package ir.code;

import ir.operand.Operand;
import ir.operand.TmpVar;

public class BinaryOp implements BasicIns{
    public enum Type {
        ADD, SUB, MUL, DIV, MOD, SGE, SGT, SLE, SLT, SEQ, SNE, AND, OR
    }


    // Originate from BinaryExp
    private Type op;
    private Operand src1;
    private Operand src2;
    private TmpVar dst;

    public BinaryOp(Type op, Operand src1, Operand src2, TmpVar dst) {
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

    public TmpVar getDst() {
        return dst;
    }
}
