package ast.stmt;

import ast.exp.ExpNode;
import ir.frame.BasicBlock;

public class BranchNode implements StmtNode {
    private final ExpNode cond;
    private final StmtNode thenStmt;
    private StmtNode elseStmt;

    public BranchNode(ExpNode cond, StmtNode thenStmt) {
        this.cond = cond;
        this.thenStmt = thenStmt;
        this.elseStmt = null;
    }

    public void setElseStmt(StmtNode elseStmt) {
        this.elseStmt = elseStmt;
    }

    public ExpNode getCond() {
        return cond;
    }

    public StmtNode getThenStmt() {
        return thenStmt;
    }

    public StmtNode getElseStmt() {
        return elseStmt;
    }

    public boolean hasElse() {
        return elseStmt != null;
    }
}
