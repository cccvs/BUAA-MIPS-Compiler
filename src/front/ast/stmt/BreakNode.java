package front.ast.stmt;

import front.lexical.Token;
import mid.frame.BasicBlock;

public class BreakNode implements StmtNode {
    private final int line;
    public BreakNode(Token token) {
        this.line = token.getLine();
    }

    public int getLine() {
        return line;
    }
}
