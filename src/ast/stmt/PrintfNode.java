package ast.stmt;

import ast.exp.ExpNode;

import java.util.ArrayList;

public class PrintfNode implements StmtNode {
    private String formatStr;
    private ArrayList<ExpNode> params;

    public PrintfNode(String formatStr) {
        this.formatStr = formatStr;
        this.params = new ArrayList<>();
    }

    public void addParam(ExpNode param) {
        params.add(param);
    }
}
