package front.ast.exp;

import front.lexical.Token;

import java.util.ArrayList;
import java.util.Iterator;

public class FuncCallNode extends ExpNode {
    private final String ident;
    private final int identLine;
    private final ArrayList<ExpNode> realParams;

    public FuncCallNode(Token token) {
        this.ident = token.getName();
        this.identLine = token.getLine();
        this.realParams = new ArrayList<>();
    }

    public void addParam(ExpNode exp) {
        realParams.add(exp);
    }

    public String getIdent() {
        return ident;
    }

    public int getIdentLine() {
        return identLine;
    }

    public Iterator<ExpNode> iterParam() {
        return realParams.iterator();
    }
}
