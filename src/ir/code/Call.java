package ir.code;

import ir.frame.FuncFrame;
import ir.operand.Operand;
import ir.operand.TmpVar;

import java.util.List;

public class Call implements BasicIns{
    // Originate from FuncCall
    private FuncFrame func;
    private List<Operand> params;
    private TmpVar ret;

    public Call(FuncFrame func, TmpVar ret) {
        this.func = func;
        this.ret = ret;
    }

    public void addParam(Operand realParam) {
        params.add(realParam);
    }
}
