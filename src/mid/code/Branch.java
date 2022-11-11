package mid.code;

import mid.frame.BasicBlock;
import mid.operand.Operand;

public class Branch implements BasicIns{
    // only list basic mips instruction
    public enum Type {
        BNEZ, BEZ,
        BEQ, BNE
    }

    private final Type type;
    private final Operand cond;
    private final BasicBlock basicBlock;

    public Branch(Type type, Operand cond, BasicBlock basicBlock) {
        this.type = type;
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
