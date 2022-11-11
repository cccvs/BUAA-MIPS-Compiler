package front.ast.stmt;

import front.ast.exp.ExpNode;

public class LoopNode implements StmtNode {
    private final ExpNode cond;
    private final StmtNode body;

    public LoopNode(ExpNode cond, StmtNode body) {
        this.cond = cond;
        this.body = body;
    }

    public ExpNode getCond() {
        return cond;
    }

    public StmtNode getBody() {
        return body;
    }
}
