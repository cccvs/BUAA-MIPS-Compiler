package front.ast.stmt;

import front.lexical.Token;

public class BreakNode implements StmtNode {
    private final int line;
    public BreakNode(Token token) {
        this.line = token.getLine();
    }

    public int getLine() {
        return line;
    }
}
