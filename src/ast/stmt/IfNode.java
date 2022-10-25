package ast.stmt;

import ast.exp.ExpNode;

public class IfNode implements StmtNode {
    private ExpNode cond;
    private StmtNode thenStmt;
    private StmtNode elseStmt;

    public IfNode(ExpNode cond, StmtNode thenStmt) {
        this.cond = cond;
        this.thenStmt = thenStmt;
        this.elseStmt = null;
    }

    public void setElseStmt(StmtNode elseStmt) {
        this.elseStmt = elseStmt;
    }
}
