package ast.stmt;

import ast.exp.ExpNode;

public class LoopNode implements StmtNode {
    private final ExpNode cond;
    private final StmtNode loop;

    public LoopNode(ExpNode cond, StmtNode loop) {
        this.cond = cond;
        this.loop = loop;
    }
}
