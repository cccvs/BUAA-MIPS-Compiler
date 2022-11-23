package mid.code;

import mid.operand.MidVar;
import mid.operand.Operand;

public class Offset implements BasicIns{
    private final MidVar dst;
    private final MidVar base;
    private final Operand offsetVal;

    public Offset(MidVar base, Operand offsetVal, MidVar dst) {
        assert dst.getRefType().equals(Operand.RefType.POINTER);
        assert offsetVal.getRefType().equals(Operand.RefType.VALUE);
        assert base.getRefType().equals(Operand.RefType.ARRAY)
                || base.getRefType().equals(Operand.RefType.POINTER);
        this.dst = dst;
        this.base = base;
        this.offsetVal = offsetVal;
    }

    @Override
    public String toString() {
        return "\tOFFSET " + dst + ", " + base + ", " + offsetVal;
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
}
