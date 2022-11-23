package front.ast.stmt;

import front.ast.exp.ExpNode;
import front.lexical.Token;
import mid.frame.BasicBlock;

public class ReturnNode implements StmtNode {
    //private int line;
    private ExpNode retVal = null;
    private final int line;
    private boolean hasExp = false;

    public ReturnNode(Token token) {
        this.line = token.getLine();
    }

    public int getLine() {
        return line;
    }

    public void setRetVal(ExpNode retVal) {
        this.retVal = retVal;
        this.hasExp = true;
    }

    public ExpNode getRetVal() {
        return retVal;
    }
}
