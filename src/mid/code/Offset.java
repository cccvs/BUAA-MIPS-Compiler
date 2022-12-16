package mid.code;

import mid.operand.MidVar;
import mid.operand.Operand;

import java.util.HashSet;
import java.util.Set;

public class Offset extends BasicIns {
    private final MidVar dst;
    private final MidVar base;
    private final Operand offsetVal;

    public Offset(MidVar base, Operand offsetVal, MidVar dst) {
        super();
        assert dst.getRefType().equals(Operand.RefType.POINTER);
        assert offsetVal.getRefType().equals(Operand.RefType.VALUE);
        assert base.getRefType().equals(Operand.RefType.ARRAY)
                || base.getRefType().equals(Operand.RefType.POINTER);
        this.dst = dst;
        this.base = base;
        this.offsetVal = offsetVal;
    }

    public MidVar getDst() {
        return dst;
    }

    public MidVar getBase() {
        return base;
    }

    public Operand getOffsetVal() {
        return offsetVal;
    }

    @Override
    public String toString() {
        return "\tOFFSET " + dst + ", " + base + ", " + offsetVal;
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
        if (base != null) {
            rightSet.add(base);
        }
        if (offsetVal instanceof MidVar) {
            rightSet.add((MidVar) offsetVal);
        }
        return rightSet;
    }
}
