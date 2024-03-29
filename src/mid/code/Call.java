package mid.code;

import mid.frame.FuncFrame;
import mid.operand.Operand;
import mid.operand.MidVar;

import java.util.*;

public class Call extends BasicIns{
    // Originate from FuncCall
    private final FuncFrame func;
    private final List<Operand> params;
    private final MidVar ret;     // null if void

    private final Set<MidVar> liveSet = new HashSet<>();

    public Call(FuncFrame func, MidVar ret) {
        super();
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

    public List<Operand> getRealParams() {
        return params;
    }

    public MidVar getRet() {
        return ret;
    }

    public void addLiveVars(Set<MidVar> set) {
        liveSet.addAll(set);
    }

    public Set<Integer> getLiveRegs() {
        Set<Integer> liveRegs = new HashSet<>();
        for (MidVar midVar : liveSet) {
            Integer reg = midVar.getReg();
            if (reg != null) {
                liveRegs.add(reg);
            }
        }
        return liveRegs;
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
        HashSet<MidVar> leftSet = new HashSet<>();
        if (ret != null) {
            leftSet.add(ret);
        }
        return leftSet;
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
