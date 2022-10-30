package ast.decl;

import ast.exp.ExpNode;
import ast.exp.NumNode;
import util.TkType;

import java.util.ArrayList;
import java.util.List;

public class DefNode {
    private final TkType varType = TkType.INTTK;
    private final boolean isConst;
    private final String ident;
    private final ArrayList<ExpNode> dimensions = new ArrayList<>();  // 0 <= dimensions.size() <= 2
    private final ArrayList<ExpNode> initValues = new ArrayList<>();

    public DefNode(boolean isConst, String ident) {
        this.isConst = isConst;
        this.ident = ident;
    }

    // syntax part
    public void addDimension(ExpNode constExpNode) {
        dimensions.add(constExpNode);
    }

    public void addInitValues(ExpNode initValue) {
        dimensions.add(initValue);
    }

    // ir part
    public void analyzeInitVal() {
        for (int i = 0; i < dimensions.size(); i++) {
            dimensions.set(i, new NumNode(dimensions.get(i).getConstVal()));
        }
        for (int i = 0; i < initValues.size(); i++) {
            initValues.set(i, new NumNode(initValues.get(i).getConstVal()));
        }
    }

    public int getConstVal(List<Integer> indexes) {
        assert dimensions.size() == indexes.size();
        int arrayBias = 0;
        for (int j = 0; j < dimensions.size(); j++) {
            arrayBias *= dimensions.get(j).getConstVal();
            arrayBias += indexes.get(j);
        }
        return initValues.get(arrayBias).getConstVal();
    }

    // basic func
    public String getIdent() {
        return ident;
    }

    public boolean isConst() {
        return isConst;
    }
}
