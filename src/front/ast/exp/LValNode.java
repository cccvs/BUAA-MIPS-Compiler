package front.ast.exp;

import mid.IrConverter;
import mid.operand.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LValNode extends ExpNode {
    private String ident;
    private final List<ExpNode> arrayIndexes;

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
    public Integer getConst() {
        Symbol symbol = IrConverter.getGlobalSym(ident);
        if (symbol.isConst()) {
            return symbol.getConstVal(arrayIndexes.stream().map(ExpNode::getConst).collect(Collectors.toList()));
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

    public List<ExpNode> getArrayIndexes() {
        return arrayIndexes;
    }
}
