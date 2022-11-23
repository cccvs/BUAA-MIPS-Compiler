package front.ast.exp;

import front.lexical.Token;
import mid.IrConverter;
import mid.operand.Symbol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class LValNode extends ExpNode {
    private final String ident;
    private final int identLine;
    private final List<ExpNode> arrayIndexes;

    public LValNode(Token leftValIdent) {
        ident = leftValIdent.getName();
        identLine = leftValIdent.getLine();
        arrayIndexes = new ArrayList<>();
    }

    public void addArrayIndex(ExpNode exp) {
        arrayIndexes.add(exp);
    }

    // basic interface
    public String getIdent() {
        return ident;
    }

    public int getIdentLine() {
        return identLine;
    }

    public Iterator<ExpNode> iterIndexExp() {
        return arrayIndexes.iterator();
    }

    public int getIndexNum() {
        return arrayIndexes.size();
    }
}
