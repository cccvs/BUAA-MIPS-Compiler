package ast.stmt;

import ast.exp.ExpNode;

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
}
