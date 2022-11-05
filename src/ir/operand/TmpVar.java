package ir.operand;

import ir.MidCode;
import ir.frame.BasicBlock;

public class TmpVar implements Operand {
    private final Integer id;
    private final Integer tmpOffset = null;
    private final RefType refType;

    public TmpVar(RefType refType) {
        assert refType.equals(RefType.VALUE) || refType.equals(RefType.POINTER);
        this.id = MidCode.genId();
        this.refType = refType;
    }

    public int getId() {
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
