package ast.exp;

import java.util.ArrayList;
import java.util.Iterator;

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

    @Override
    public Integer getConst() {
        System.exit(4);
        return 0;
    }

    public String getIdent() {
        return ident;
    }

    public Iterator<ExpNode> iterParam() {
        return realParams.iterator();
    }
}
