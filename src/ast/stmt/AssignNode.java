package ast.stmt;

import ast.exp.ExpNode;
import ast.exp.LValNode;

public class AssignNode implements StmtNode {
    private boolean getInt;
    private LValNode left;
    private ExpNode right;

    public AssignNode(LValNode left) {
        this.left = left;
        this.right = null;
        this.getInt = false;
    }

    public void setRight(ExpNode right) {
        this.right = right;
        this.getInt = false;
    }

    public void setGetInt() {
        this.right = null;
        this.getInt = true;
    }
}
