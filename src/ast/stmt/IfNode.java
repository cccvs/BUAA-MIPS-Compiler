package ast.stmt;

import ast.exp.ExpNode;
import ir.frame.BasicBlock;

public class IfNode implements StmtNode {
    private ExpNode cond;
    private StmtNode thenStmt;
    private StmtNode elseStmt;

    public IfNode(ExpNode cond, StmtNode thenStmt) {
        this.cond = cond;
        this.thenStmt = thenStmt;
        this.elseStmt = null;
    }

    // ir part
    public void toIr(BasicBlock basicBlock) {

    }

    public void setElseStmt(StmtNode elseStmt) {
        this.elseStmt = elseStmt;
    }
}
