package ast.exp;

import ast.stmt.StmtNode;
import ir.code.BasicIns;
import ir.frame.BasicBlock;
import ir.operand.Operand;

import java.util.List;

public interface ExpNode extends StmtNode {
    public Integer getConst();
    public Operand expToIr(BasicBlock basicBlock);
}
