package ast.stmt;

import ast.exp.ExpNode;
import ast.exp.LValNode;
import ir.frame.BasicBlock;
import ir.operand.Operand;

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
