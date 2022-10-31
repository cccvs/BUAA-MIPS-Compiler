package ast.exp;

import ir.MidCode;
import ir.code.Call;
import ir.frame.BasicBlock;
import ir.frame.FuncFrame;
import ir.operand.Operand;
import ir.operand.TmpVar;

import java.util.ArrayList;

public class FuncCallNode implements ExpNode {
    private String ident;
    private ArrayList<ExpNode> realParams;

    public FuncCallNode(String ident) {
        this.ident = ident;
        this.realParams = new ArrayList<>();
    }

    public void addParam(ExpNode exp) {
        realParams.add(exp);
    }

    // ir part
    @Override
    public Operand expToIr(BasicBlock basicBlock) {
        FuncFrame func = MidCode.getFunc(ident);
        // call part
        TmpVar recv = null;
        if (func.getRetType().equals(FuncFrame.RetType.INT)) {
            recv = new TmpVar(basicBlock);
        }
        Call call = new Call(func, recv);
        // params part
        for (ExpNode realParam : realParams) {
            call.addParam(realParam.expToIr(basicBlock));
        }
        // add and ret
        basicBlock.addIns(call);
        return recv;    // may be null
    }

    @Override
    public Integer getConst() {
        return 0;
    }
}
