package ast.stmt;

import ast.exp.ExpNode;
import ir.frame.BasicBlock;

public class ReturnNode implements StmtNode {
    //private int line;
    private ExpNode retVal = null;
    private boolean hasExp = false;

    public ReturnNode() {

    }

    // ir part
    public void toIr(BasicBlock basicBlock) {
        // TODO[5]: 1031, fill return convert
    }

    public void setRetVal(ExpNode retVal) {
        this.retVal = retVal;
        this.hasExp = true;
    }

    public ExpNode getRetVal() {
        return retVal;
    }
}
