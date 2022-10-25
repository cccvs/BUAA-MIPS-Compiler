package ast.exp;

import util.TkType;

public class BinaryExpNode implements ExpNode {
    private TkType operand;
    private ExpNode leftExp;
    private ExpNode rightExp;

    public BinaryExpNode(TkType operand, ExpNode leftExp, ExpNode rightExp) {
        this.operand = operand;
        this.leftExp = leftExp;
        this.rightExp = rightExp;
    }
}
