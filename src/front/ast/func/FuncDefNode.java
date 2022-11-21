package front.ast.func;

import front.ast.stmt.BlockNode;
import front.TkType;

import java.util.ArrayList;
import java.util.Iterator;

public class FuncDefNode {
    private TkType funcType;
    private String ident;
    private ArrayList<FuncFParamNode> params;
    private BlockNode block;

    // INT, "main" for main function
    public FuncDefNode(TkType type, String ident) {
        this.funcType = type;
        this.ident = ident;
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
