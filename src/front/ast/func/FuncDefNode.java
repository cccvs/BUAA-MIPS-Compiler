package front.ast.func;

import front.ast.stmt.BlockNode;
import front.TkType;
import front.lexical.Token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FuncDefNode {
    private final TkType funcType;
    private final String ident;
    private final int identLine;
    private final List<FuncFParamNode> params;
    private BlockNode block;

    // INT, "main" for main function
    public FuncDefNode(TkType type, Token identToken) {
        this.funcType = type;
        this.ident = identToken.getName();
        this.identLine = identToken.getLine();
        this.params = new ArrayList<>();
        this.block = null;
        assert type.equals(TkType.INTTK) || type.equals(TkType.VOIDTK);
    }

    // basic
    public void addParam(FuncFParamNode param) {
        params.add(param);
    }

    public void setBlock(BlockNode block) {
        this.block = block;
    }

    public String getIdent() {
        return ident;
    }

    public int getIdentLine() {
        return identLine;
    }

    public TkType getFuncType() {
        return funcType;
    }

    public Iterator<FuncFParamNode> paramIter() {
        return params.iterator();
    }

    public BlockNode getBlock() {
        return block;
    }
}
