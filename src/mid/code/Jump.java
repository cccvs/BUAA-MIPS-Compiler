package mid.code;

import mid.frame.BasicBlock;

public class Jump implements BasicIns{
    private final BasicBlock basicBlock;

    public Jump(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }

    public String getLabel() {
        return basicBlock.getLabel();
    }
}
