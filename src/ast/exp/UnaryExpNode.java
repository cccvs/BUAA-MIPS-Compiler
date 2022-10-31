package ast.exp;

import ir.code.UnaryOp;
import ir.frame.BasicBlock;
import ir.operand.Operand;
import ir.operand.TmpVar;
import util.TkType;

import java.util.HashMap;

public class UnaryExpNode implements ExpNode {
    @FunctionalInterface
    private interface UnaryCal {
        int cal(int x);
    }

    private TkType op;
    private ExpNode exp;
    private final HashMap<TkType, UnaryCal> opMap = new HashMap<TkType, UnaryCal>() {{
        put(TkType.PLUS, x -> x);
        put(TkType.MINU, x -> -x);
        put(TkType.NOT, x -> (x != 0) ? 0 : 1);
    }};
    private final HashMap<TkType, UnaryOp.Type> typeMap = new HashMap<TkType, UnaryOp.Type>() {{
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

    @Override
    public Operand expToIr(BasicBlock basicBlock) {
        Operand src = expToIr(basicBlock);
        if (op.equals(TkType.PLUS)) {
            return src;
        } else {
            TmpVar dst = new TmpVar(basicBlock);
            UnaryOp unaryOp = new UnaryOp(typeMap.get(op), src, dst);
            basicBlock.addIns(unaryOp);
            return dst;
        }
    }
}
