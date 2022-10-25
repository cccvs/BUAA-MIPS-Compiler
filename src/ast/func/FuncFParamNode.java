package ast.func;

import ast.exp.ExpNode;

import java.util.ArrayList;

public class FuncFParamNode {
    private String ident;
    private ArrayList<ExpNode> dimensions;

    public FuncFParamNode(String ident) {
        this.ident = ident;
        this.dimensions = new ArrayList<>();
    }

    public void addDimension(ExpNode dimension) {
        dimensions.add(dimension);
    }
}
