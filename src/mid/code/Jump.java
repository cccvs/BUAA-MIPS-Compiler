package mid.code;

import mid.frame.BasicBlock;
import mid.operand.MidVar;

import java.util.HashSet;
import java.util.Set;

public class Jump implements BasicIns{
    private final BasicBlock basicBlock;

    public Jump(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }

    public BasicBlock getTargetBlock() {
        return basicBlock;
    }

    @Override
    public String toString() {
        return "\tJUMP " + basicBlock.getLabel();
    }

    @Override
    public Set<MidVar> leftSet() {
        return new HashSet<>();
    }

    @Override
    public Set<MidVar> rightSet() {
        return new HashSet<>();
    }
}
