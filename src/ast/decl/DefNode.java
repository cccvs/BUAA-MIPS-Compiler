package ast.decl;

import ast.exp.ExpNode;

import java.util.ArrayList;

public class DefNode {
    private final boolean isConst;
    private String ident;
    private ArrayList<ExpNode> dimensions;  // 0 <= dimensions.size() <= 2
    private ArrayList<ExpNode> initValues;

    public DefNode(boolean isConst, String ident) {
        this.isConst = isConst;
        this.ident = ident;
        this.dimensions = new ArrayList<>();
        this.initValues = new ArrayList<>();
    }

    public void addDimension(ExpNode constExpNode) {
        dimensions.add(constExpNode);
    }

    public void addInitValues(ExpNode initValue) {
        dimensions.add(initValue);
    }

}
