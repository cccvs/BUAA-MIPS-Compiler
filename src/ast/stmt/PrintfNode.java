package ast.stmt;

import ast.exp.ExpNode;
import ir.frame.BasicBlock;

import java.util.ArrayList;

public class PrintfNode implements StmtNode {
    private String formatStr;
    private ArrayList<ExpNode> params;

    public PrintfNode(String formatStr) {
        this.formatStr = formatStr;
        this.params = new ArrayList<>();
    }

    // ir part
    public void toIr(BasicBlock basicBlock) {
        // TODO[4]: 1031, convert printf
    }
    // basic method
    public void addParam(ExpNode param) {
        params.add(param);
    }
}
