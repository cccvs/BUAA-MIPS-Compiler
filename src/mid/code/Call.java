package mid.code;

import mid.frame.FuncFrame;
import mid.operand.Operand;
import mid.operand.MidVar;

import java.util.*;

public class Call implements BasicIns{
    // Originate from FuncCall
    private final FuncFrame func;
    private final List<Operand> params;
    private final MidVar ret;     // null if void

    public Call(FuncFrame func, MidVar ret) {
        this.func = func;
        this.params = new ArrayList<>();
        this.ret = ret;
    }

    public void addParam(Operand realParam) {
        params.add(realParam);
    }

    public FuncFrame getFunc() {
        return func;
    }

    public Iterator<Operand> iterRealParam() {
        return params.iterator();
    }

    public MidVar getRet() {
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\tCALL ").append(func.getLabel()).append("(");
        sb.append(params.stream().map(Operand::toString).reduce((x, y) -> x + ", " + y).orElse(""));
        sb.append(")");
        if (ret != null) {
            sb.append(" -> ");
            sb.append(ret);
        }
        return sb.toString();
    }

    @Override
    public Set<MidVar> leftSet() {
        return new HashSet<>();
    }

    @Override
    public Set<MidVar> rightSet() {
        Set<MidVar> rightSet = new HashSet<>();
        for (Operand param : params) {
            if (param instanceof MidVar) {
                rightSet.add((MidVar) param);
            }
        }
        return rightSet;
    }
}
