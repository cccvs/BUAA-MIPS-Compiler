package ast.stmt;

import ast.exp.ExpNode;
import ir.frame.BasicBlock;

public class LoopNode implements StmtNode {
    private final ExpNode cond;
    private final StmtNode loop;

    // ir part
    public void toIr(BasicBlock basicBlock) {

    }

    public LoopNode(ExpNode cond, StmtNode loop) {
        this.cond = cond;
        this.loop = loop;
    }
}
