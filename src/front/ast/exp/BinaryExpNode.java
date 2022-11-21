package front.ast.exp;

import mid.code.BinaryOp;
import front.TkType;

import java.util.HashMap;

public class BinaryExpNode extends ExpNode {
    @FunctionalInterface
    private interface BinaryCal {
        int cal(int x, int y);
    }

    private final TkType op;
    private final ExpNode leftExp;
    private final ExpNode rightExp;
    private static final HashMap<TkType, BinaryCal> opMap = new HashMap<TkType, BinaryCal>() {{
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
    private static final HashMap<TkType, BinaryOp.Type> typeMap = new HashMap<TkType, BinaryOp.Type>() {{
        // AddExp
        put(TkType.PLUS, BinaryOp.Type.ADD);
        put(TkType.MINU, BinaryOp.Type.SUB);
        // MulExp
        put(TkType.MULT, BinaryOp.Type.MUL);
        put(TkType.DIV, BinaryOp.Type.DIV);
        put(TkType.MOD, BinaryOp.Type.MOD);
        // RelExp
        put(TkType.GEQ, BinaryOp.Type.SGE);
        put(TkType.GRE, BinaryOp.Type.SGT);
        put(TkType.LEQ, BinaryOp.Type.SLE);
        put(TkType.LSS, BinaryOp.Type.SLT);
        // EqExp
        put(TkType.EQL, BinaryOp.Type.SEQ);
        put(TkType.NEQ, BinaryOp.Type.SNE);
        // AndExp, OrExp
        // logical and/or
        put(TkType.AND, BinaryOp.Type.AND);
        put(TkType.OR, BinaryOp.Type.OR);
    }};

    public BinaryExpNode(TkType op, ExpNode leftExp, ExpNode rightExp) {
        this.op = op;
        this.leftExp = leftExp;
        this.rightExp = rightExp;
    }

    // ir part
    @Override
    public Integer getConst() {
        BinaryCal binaryCal = opMap.get(op);
        return binaryCal.cal(leftExp.getConst(), rightExp.getConst());
    }

    public TkType getOp() {
        return op;
    }

    public ExpNode getLeftExp() {
        return leftExp;
    }

    public ExpNode getRightExp() {
        return rightExp;
    }

    public static BinaryOp.Type typeMap(TkType op) {
        return typeMap.get(op);
    }

    public boolean isOrLink() {
        return op.equals(TkType.OR);
    }

    public boolean isAndLink() {
        return op.equals(TkType.AND);
    }

    public boolean isEqLink() {
        return op.equals(TkType.EQL) || op.equals(TkType.NEQ);
    }

    public boolean isRelLink() {
        return op.equals(TkType.GEQ) || op.equals(TkType.GRE)
                || op.equals(TkType.LEQ) || op.equals(TkType.LSS);
    }
}
