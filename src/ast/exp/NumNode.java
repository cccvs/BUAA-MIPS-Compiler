package ast.exp;

import ir.code.BasicIns;
import ir.frame.BasicBlock;
import ir.operand.Imm;
import ir.operand.Operand;

import java.util.List;

public class NumNode implements ExpNode {
    private final int num;

    public NumNode(int num) {
        this.num = num;
    }

    @Override
    public Operand expToIr(BasicBlock basicBlock) {
        return new Imm(num);
    }

    public Integer getConst() {
        return num;
    }
}
