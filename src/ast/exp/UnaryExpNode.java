package ast.exp;

import util.TkType;

import java.util.HashMap;

public class UnaryExpNode implements ExpNode {
    @FunctionalInterface
    private interface UnaryCal {
        int cal(int x);
    }

    private TkType unaryOp;
    private ExpNode exp;
    private final HashMap<TkType, UnaryCal> opMap = new HashMap<TkType, UnaryCal>() {{
        put(TkType.PLUS, x -> x);
        put(TkType.MINU, x -> -x);
        put(TkType.NOT, x -> (x != 0) ? 0 : 1);
    }};

    public UnaryExpNode(TkType unaryOp, ExpNode exp) {
        this.unaryOp = unaryOp;
        this.exp = exp;
    }

    @Override
    public Integer getConstVal() {
        UnaryCal unaryCal = opMap.get(unaryOp);
        return unaryCal.cal(exp.getConstVal());
    }
}
