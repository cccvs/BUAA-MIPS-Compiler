package front.ast.exp;

import mid.code.UnaryOp;
import util.TkType;

import java.util.HashMap;

public class UnaryExpNode extends ExpNode {
    @FunctionalInterface
    private interface UnaryCal {
        int cal(int x);
    }

    private final TkType op;
    private final ExpNode exp;
    private final static HashMap<TkType, UnaryCal> opMap = new HashMap<TkType, UnaryCal>() {{
        put(TkType.PLUS, x -> x);
        put(TkType.MINU, x -> -x);
        put(TkType.NOT, x -> (x != 0) ? 0 : 1);
    }};
    private final static HashMap<TkType, UnaryOp.Type> typeMap = new HashMap<TkType, UnaryOp.Type>() {{
        put(TkType.MINU, UnaryOp.Type.NEG);
        put(TkType.NOT, UnaryOp.Type.NOT);
    }};

    public UnaryExpNode(TkType op, ExpNode exp) {
        this.op = op;
        this.exp = exp;
    }

    // ir part
    @Override
    public Integer getConst() {
        UnaryCal unaryCal = opMap.get(op);
        return unaryCal.cal(exp.getConst());
    }

    public TkType getOp() {
        return op;
    }

    public ExpNode getExp() {
        return exp;
    }

    public static UnaryOp.Type typeMap(TkType type) {
        return typeMap.get(type);
    }
}
