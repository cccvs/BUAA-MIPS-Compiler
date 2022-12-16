package mid.code;

import mid.operand.MidVar;
import mid.operand.Operand;
import mid.operand.Symbol;

import java.util.HashSet;
import java.util.Set;

public class MemOp extends BasicIns {
    // Originate from Assign, Def/Decl, Exp(LVal)
    public enum Type {
        LOAD,
        STORE
    }

    private final Type op;
    private final Operand value;
    private final MidVar pointer;

    public MemOp(Type op, Operand value, MidVar pointer) {
        super();
        assert pointer.getRefType().equals(Operand.RefType.POINTER);
        this.op = op;
        this.value = value;
        this.pointer = pointer;
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

    @Override
    public String toString() {
        return "\t" + op.name() + " " + value + ", " + pointer;
    }

    @Override
    public Set<MidVar> leftSet() {
        return new HashSet<>();
    }

    @Override
    public Set<MidVar> rightSet() {
        Set<MidVar> rightSet = new HashSet<>();
        if (value instanceof MidVar) {
            rightSet.add((MidVar) value);
        }
        if (pointer != null) {
            rightSet.add(pointer);
        }
        return rightSet;
    }
}
