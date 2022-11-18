package mid.code;

import mid.operand.MidVar;
import mid.operand.Operand;
import mid.operand.Symbol;

public class MemOp implements BasicIns{
    // Originate from Assign, Def/Decl, Exp(LVal)
    public enum Type {
        LOAD,
        STORE
    }

    private final Type op;
    private final Operand value;
    private final MidVar pointer;

    public MemOp(Type op, Operand value, MidVar pointer) {
        assert pointer.getRefType().equals(Operand.RefType.POINTER);
        this.op = op;
        this.value = value;
        this.pointer = pointer;
    }

    @Override
    public String toString() {
        return "\t" + op.name() + " " + value + ", " + pointer;
    }

    public Type getOp() {
        return op;
    }

    public MidVar getPointer() {
        return pointer;
    }

    public Operand getValue() {
        return value;
    }
}
