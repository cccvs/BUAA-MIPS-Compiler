package ast.func;

import ast.exp.ExpNode;

import java.util.ArrayList;

public class FuncFParamNode {
    private String ident;
    private boolean isPointer;
    private ArrayList<ExpNode> dimensions;  // 0 <= dimensions.size() <= 1

    public FuncFParamNode(String ident) {
        this.ident = ident;
        this.isPointer = false;
        this.dimensions = new ArrayList<>();
    }

    // to sign whether the parameter is pointer
    public void setPointer(boolean pointer) {
        isPointer = pointer;
    }

    public void addDimension(ExpNode dimension) {
        dimensions.add(dimension);
    }
}
