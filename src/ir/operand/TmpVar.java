package ir.operand;

import ir.MidCode;
import ir.frame.BasicBlock;

public class TmpVar implements Operand{
    private final Integer id;
    private final Integer tmpOffset = null;

    public TmpVar() {
        this.id = MidCode.genId();
    }

    public int getId() {
        return id;
    }

    public int getTmpOffset() {
        return tmpOffset;
    }

    @Override
    public String toString() {
        return "t" + id + "[v]";
    }
}
