package ast.stmt;

import ast.exp.ExpNode;
import ast.exp.LValNode;
import ir.code.Input;
import ir.frame.BasicBlock;
import ir.operand.Operand;
import ir.operand.TmpVar;

public class AssignNode implements StmtNode {
    private boolean getInt;
    private LValNode leftVal;
    private ExpNode exp;

    public AssignNode(LValNode leftVal) {
        this.leftVal = leftVal;
        this.exp = null;
        this.getInt = false;
    }

    // ir part
    public void toIr(BasicBlock basicBlock) {
        Operand recv;
        // TODO[3]: 1031, design array op interface
    }

    // basic method
    public void setExp(ExpNode exp) {
        this.exp = exp;
        this.getInt = false;
    }

    public void setGetInt() {
        this.exp = null;
        this.getInt = true;
    }
}
