package ir.code;

import ir.frame.BasicBlock;
import ir.operand.Operand;

public class Branch implements BasicIns{
    // Originate from If, Loop, BinaryExp(&&, ||)
    public enum Type {
        BEZ
    }

    private final Operand cond;
    private final BasicBlock basicBlock;

    public Branch(Operand cond, BasicBlock basicBlock) {
        this.cond = cond;
        this.basicBlock = basicBlock;
    }

    public String getLabel() {
        return basicBlock.getLabel();
    }

    public Operand getCond() {
        return cond;
    }
}
