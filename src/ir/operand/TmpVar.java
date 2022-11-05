package ir.operand;

import ir.MidCode;

public class TmpVar implements Operand {
    private final Integer id;
    private Integer stackOffset = null;
    private final RefType refType;

    public TmpVar(RefType refType) {
        assert refType.equals(RefType.VALUE) || refType.equals(RefType.POINTER);
        this.id = MidCode.genId();
        this.refType = refType;
    }

    public void setStackOffset(int stackOffset) {
        assert this.stackOffset == null;
        this.stackOffset = stackOffset;
    }

    public Integer getOffset() {
        return stackOffset;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public RefType getRefType() {
        return refType;
    }

    @Override
    public String toString() {
        String typeStr = refType.name().substring(0, 1).toLowerCase();
        return "t" + id + "[" + typeStr + "]";
    }
}
