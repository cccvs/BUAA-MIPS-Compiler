package ast.exp;

import util.TkType;

import java.util.HashMap;

public class BinaryExpNode implements ExpNode {
    @FunctionalInterface
    private interface BinaryCal {
        int cal(int x, int y);
    }

    private TkType operand;
    private ExpNode leftExp;
    private ExpNode rightExp;
    private final HashMap<TkType, BinaryCal> opMap = new HashMap<TkType, BinaryCal>() {{
            // AddExp
            put(TkType.PLUS, Integer::sum);
            put(TkType.MINU, (a, b) -> a - b);
            // MulExp
            put(TkType.MULT, (a, b) -> a * b);
            put(TkType.DIV, (a, b) -> a / b);
            put(TkType.MOD, (a, b) -> a % b);
            // RelExp
            put(TkType.GEQ, (a, b) -> (a >= b) ? 1 : 0);
            put(TkType.GRE, (a, b) -> (a > b) ? 1 : 0);
            put(TkType.LEQ, (a, b) -> (a <= b) ? 1 : 0);
            put(TkType.LSS, (a, b) -> (a < b) ? 1 : 0);
            // EqExp
            put(TkType.EQL, (a, b) -> (a == b) ? 1 : 0);
            put(TkType.NEQ, (a, b) -> (a != b) ? 1 : 0);
            // AndExp, OrExp
            put(TkType.AND, (a, b) -> ((a != 0) && (b != 0)) ? 1 : 0);
            put(TkType.OR, (a, b) -> ((a != 0) || (b != 0)) ? 1 : 0);
        }};

    public BinaryExpNode(TkType operand, ExpNode leftExp, ExpNode rightExp) {
        this.operand = operand;
        this.leftExp = leftExp;
        this.rightExp = rightExp;
    }

    @Override
    public Integer getConstVal() {
        BinaryCal binaryCal = opMap.get(operand);
        return binaryCal.cal(leftExp.getConstVal(), rightExp.getConstVal());
    }
}
