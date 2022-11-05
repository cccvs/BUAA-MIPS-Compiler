package ir.operand;

import ir.MidCode;

public class TmpVar implements Operand {
    private final Integer id;
    private final Integer tmpOffset = null;
    private final RefType refType;

    public TmpVar(RefType refType) {
        assert refType.equals(RefType.VALUE) || refType.equals(RefType.POINTER);
        this.id = MidCode.genId();
        this.refType = refType;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public int getTmpOffset() {
        return tmpOffset;
    }

    @Override
    public String toString() {
        String typeStr = refType.name().substring(0, 1).toLowerCase();
        return "t" + id + "[" + typeStr + "]";
    }

    @Override
    public RefType getRefType() {
        return refType;
    }
}
