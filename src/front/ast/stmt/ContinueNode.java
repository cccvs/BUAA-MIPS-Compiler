package front.ast.stmt;

import front.lexical.Token;

public class ContinueNode implements StmtNode {
    private final int line;

    public ContinueNode(Token token) {
        this.line = token.getLine();
    }

    public int getLine() {
        return line;
    }
}
