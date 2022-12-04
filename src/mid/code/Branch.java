package mid.code;

import mid.frame.BasicBlock;
import mid.operand.MidVar;
import mid.operand.Operand;

import java.util.HashSet;
import java.util.Set;

public class Branch implements BasicIns{
    // only list basic mips instruction
    public enum Type {
        BNEZ
        //, BEZ,
        //BEQ, BNE
    }

    private final Type type;
    private final Operand cond;
    private final BasicBlock blockTrue;
    private final BasicBlock blockFalse;

    public Branch(Type type, Operand cond, BasicBlock trueBlock, BasicBlock blockFalse) {
        this.type = type;
        this.cond = cond;
        this.blockTrue = trueBlock;
        this.blockFalse = blockFalse;
    }

    public Type getType() {
        return type;
    }

    public BasicBlock getBlockTrue() {
        return blockTrue;
    }

    public BasicBlock getBlockFalse() {
        return blockFalse;
    }

    public Operand getCond() {
        return cond;
    }

    @Override
    public String toString() {
        return "\t" + type.name() + " " + cond.toString() + " " +
                blockTrue.getLabel() + ", " + blockFalse.getLabel();
    }

    @Override
    public Set<MidVar> leftSet() {
        return new HashSet<>();
    }

    @Override
    public Set<MidVar> rightSet() {
        Set<MidVar> rightSet = new HashSet<>();
        if (cond instanceof MidVar) {
            rightSet.add((MidVar) cond);
        }
        return rightSet;
    }
}
