package front.ast.func;

import front.ast.exp.ExpNode;
import front.lexical.Token;

import java.util.ArrayList;
import java.util.List;

public class FuncFParamNode {
    private final String ident;
    private final int identLine;
    private boolean isPointer;
    private final List<ExpNode> dimensions;  // 0 <= dimensions.size() <= 1

    public FuncFParamNode(Token identToken) {
        this.ident = identToken.getName();
        this.identLine = identToken.getLine();
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

    public String getIdent() {
        return ident;
    }

    public int getLine() {
        return identLine;
    }

    public boolean isPointer() {
        return isPointer;
    }

    public List<ExpNode> getDimensions() {
        return dimensions;
    }
}
