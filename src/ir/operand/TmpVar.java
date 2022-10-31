package ir.operand;

import ir.MidCode;
import ir.frame.BasicBlock;

public class TmpVar implements Operand{
    private final int id;
    private final int tmpOffset;

    public TmpVar(BasicBlock basicBlock) {
        this.id = MidCode.genTagId();
        this.tmpOffset = basicBlock.getTmpOffset();
        basicBlock.putTmp(this);
    }

    public int getId() {
        return id;
    }

    public int getTmpOffset() {
        return tmpOffset;
    }

    @Override
    public String toString() {
        return "t_" + id;
    }
}
