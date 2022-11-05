package ir.code;

import ir.frame.FuncFrame;
import ir.operand.Operand;
import ir.operand.MidVar;

import java.util.ArrayList;
import java.util.List;

public class Call implements BasicIns{
    // Originate from FuncCall
    private FuncFrame func;
    private List<Operand> params;
    private MidVar ret;

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
}
