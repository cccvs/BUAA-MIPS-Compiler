package mid.code;

import mid.operand.Operand;
import mid.operand.MidVar;

import java.util.HashSet;
import java.util.Set;

public class BinaryOp implements BasicIns {
    public enum Type {
        ADD, SUB, MUL, DIV, MOD, SGE, SGT, SLE, SLT, SEQ, SNE, AND, OR
    }


    // Originate from BinaryExp
    private final Type op;
    private final Operand src1;
    private final Operand src2;
    private final MidVar dst;

    public BinaryOp(Type op, Operand src1, Operand src2, MidVar dst) {
        this.op = op;
        this.src1 = src1;
        this.src2 = src2;
        this.dst = dst;
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

    @Override
    public String toString() {
        return "\t" + op.name() + " " + dst + ", " + src1 + ", " + src2;
    }

    @Override
    public Set<MidVar> leftSet() {
        Set<MidVar> leftSet = new HashSet<>();
        if (dst != null) {
            leftSet.add(dst);
        }
        return leftSet;
    }

    @Override
    public Set<MidVar> rightSet() {
        Set<MidVar> rightSet = new HashSet<>();
        if (src1 instanceof MidVar) {
            rightSet.add((MidVar) src1);
        }
        if (src2 instanceof MidVar) {
            rightSet.add((MidVar) src2);
        }
        return rightSet;
    }
}
