package ast.exp;

import java.util.ArrayList;

public class FuncCallNode implements ExpNode {
    private String ident = null;
    private ArrayList<ExpNode> realParams;

    public FuncCallNode(String ident) {
        this.ident = ident;
        this.realParams = new ArrayList<>();
    }

    public void addParam(ExpNode exp) {
        realParams.add(exp);
    }
}
