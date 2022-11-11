package mid.code;

import mid.operand.MidVar;
import mid.operand.Operand;

public class UnaryOp implements BasicIns{
    // Originate from UnaryExp
    public enum Type {
        MOV,
        NEG,
        NOT     // logic not
    }

    private Type op;
    private Operand src;
    private MidVar dst;

    public UnaryOp(Type op, Operand src, MidVar dst) {
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
}
