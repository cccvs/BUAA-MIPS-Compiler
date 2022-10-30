package ast.exp;

import ir.MidCode;
import ast.decl.DefNode;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class LValNode implements ExpNode {
    private String ident;
    private final ArrayList<ExpNode> arrayIndexes;

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

    // ir part
    public Integer getConstVal() {
        DefNode defNode = MidCode.getSymbol(ident);
        if (defNode.isConst()) {
            return defNode.getConstVal(arrayIndexes.stream().map(ExpNode::getConstVal).collect(Collectors.toList()));
        } else {
            System.out.println("expect Const or Num, get Var!");
            System.exit(3);
            return null;
        }
    }

    // basic interface
    public String getIdent() {
        return ident;
    }
}
