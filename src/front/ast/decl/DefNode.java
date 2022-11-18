package front.ast.decl;

import front.ast.exp.ExpNode;
import util.TkType;

import java.util.ArrayList;
import java.util.List;

public class DefNode {
    private final TkType varType = TkType.INTTK;
    private final boolean isConst;
    private final String ident;
    private final List<ExpNode> dimensions = new ArrayList<>();  // 0 <= dimensions.size() <= 2
    private final List<ExpNode> initValues = new ArrayList<>();

    public DefNode(boolean isConst, String ident) {
        this.isConst = isConst;
        this.ident = ident;
    }

    // front part
    public void addDimension(ExpNode constExpNode) {
        dimensions.add(constExpNode);
    }

    public void addInitValues(ExpNode initValue) {
        initValues.add(initValue);
    }

    // basic func
    public String getIdent() {
        return ident;
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