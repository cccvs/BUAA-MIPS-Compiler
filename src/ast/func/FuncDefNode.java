package ast.func;

import ast.stmt.BlockNode;
import util.TkType;

import java.util.ArrayList;

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

    public void addParam(FuncFParamNode param) {
        params.add(param);
    }

    public void setBlock(BlockNode block) {
        this.block = block;
    }
}
