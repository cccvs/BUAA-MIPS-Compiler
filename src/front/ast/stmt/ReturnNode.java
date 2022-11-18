package front.ast.stmt;

import front.ast.exp.ExpNode;
import mid.frame.BasicBlock;

public class ReturnNode implements StmtNode {
    //private int line;
    private ExpNode retVal = null;
    private boolean hasExp = false;

    public ReturnNode() {

    }

    public void setRetVal(ExpNode retVal) {
        this.retVal = retVal;
        this.hasExp = true;
    }

    public ExpNode getRetVal() {
        return retVal;
    }
}
