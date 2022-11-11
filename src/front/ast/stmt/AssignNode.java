package front.ast.stmt;

import front.ast.exp.ExpNode;
import front.ast.exp.LValNode;

public class AssignNode implements StmtNode {
    private boolean getInt;
    private LValNode leftVal;
    private ExpNode exp;

    public AssignNode(LValNode leftVal) {
        this.leftVal = leftVal;
        this.exp = null;
        this.getInt = false;
    }

    // basic method
    public void setExp(ExpNode exp) {
        this.exp = exp;
        this.getInt = false;
    }

    public void setGetInt() {
        this.exp = null;
        this.getInt = true;
    }

    public boolean isGetInt() {
        return getInt;
    }

    public LValNode getLeftVal() {
        return leftVal;
    }

    public ExpNode getExp() {
        return exp;
    }
}
