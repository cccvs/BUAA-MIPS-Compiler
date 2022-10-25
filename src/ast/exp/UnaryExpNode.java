package ast.exp;

import util.TkType;

public class UnaryExpNode implements ExpNode {
    private TkType unaryOp;
    private ExpNode exp;

    public UnaryExpNode(TkType unaryOp, ExpNode exp) {
        this.unaryOp = unaryOp;
        this.exp = exp;
    }
}
