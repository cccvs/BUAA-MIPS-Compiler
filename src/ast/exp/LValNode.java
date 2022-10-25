package ast.exp;

import java.util.ArrayList;

public class LValNode implements ExpNode {
    private String ident;
    private ArrayList<ExpNode> arrayIndexes;

    public LValNode() {
        ident = null;
        arrayIndexes = new ArrayList<>();
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public void addArrayIndex(ExpNode exp) {
        arrayIndexes.add(exp);
    }
}
