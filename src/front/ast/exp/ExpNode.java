package front.ast.exp;

import front.ast.stmt.StmtNode;

public abstract class ExpNode implements StmtNode {

    public boolean isOrBinary() {
        return (this instanceof BinaryExpNode) && ((BinaryExpNode) this).isOrLink();
    }

    public boolean isAndBinary() {
        return (this instanceof BinaryExpNode) && ((BinaryExpNode) this).isAndLink();
    }

    public boolean isEqBinary() {
        return (this instanceof BinaryExpNode) && ((BinaryExpNode) this).isEqLink();
    }

    public boolean isRelBinary() {
        return (this instanceof BinaryExpNode) && ((BinaryExpNode) this).isRelLink();
    }
}
