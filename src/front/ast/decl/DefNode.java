package front.ast.decl;

import front.ast.exp.ExpNode;
import front.TkType;
import front.lexical.Token;

import java.util.ArrayList;
import java.util.List;

public class DefNode {
    private final TkType varType = TkType.INTTK;
    private final boolean isConst;
    private final String ident;
    private final int identLine;
    private final List<ExpNode> dimensions = new ArrayList<>();  // 0 <= dimensions.size() <= 2
    private final List<ExpNode> initValues = new ArrayList<>();
    private final boolean isGetInt;

    public DefNode(boolean isConst, Token identToken, boolean isGetInt) {
        this.isConst = isConst;
        this.ident = identToken.getName();
        this.identLine = identToken.getLine();
        this.isGetInt = isGetInt;
    }

    // front part
    public void addDimension(ExpNode constExpNode) {
        dimensions.add(constExpNode);
    }

    public void addInitValues(ExpNode initValue) {
        initValues.add(initValue);
    }

    public boolean isGetInt() {
        return isGetInt;
    }

    // basic func
    public String getIdent() {
        return ident;
    }

    public int getIdentLine() {
        return identLine;
    }

    public boolean isConst() {
        return isConst;
    }

    public List<ExpNode> getDimensions() {
        return dimensions;
    }

    public List<ExpNode> getInitValues() {
        return initValues;
    }
}
