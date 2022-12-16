package mid.code;

import mid.operand.MidVar;
import mid.operand.Operand;

import java.util.HashSet;
import java.util.Set;

public class UnaryOp extends BasicIns{
    // Originate from UnaryExp
    public enum Type {
        MOV,
        NEG,
        NOT     // logic not
    }

    private final Type op;
    private final Operand src;
    private final MidVar dst;

    public UnaryOp(Type op, Operand src, MidVar dst) {
        super();
        this.op = op;
        this.src = src;
        this.dst = dst;
    }

    public Type getOp() {
        return op;
    }

    public Operand getSrc() {
        return src;
    }

    public MidVar getDst() {
        return dst;
    }

    @Override
    public String toString() {
        return "\t" + op.name() + " " + dst + ", " + src;
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
        if (src instanceof MidVar) {
            rightSet.add((MidVar) src);
        }
        return rightSet;
    }
}
