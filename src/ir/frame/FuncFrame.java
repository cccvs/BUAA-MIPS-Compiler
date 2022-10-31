package ir.frame;

import ast.func.FuncDefNode;
import ast.func.FuncFParamNode;
import ir.operand.Symbol;
import util.TkType;

import java.util.ArrayList;
import java.util.List;

public class FuncFrame {
    /*
     *  Originate from FuncDefNode.
     *  Can't extend BasicBlock. Otherwise, it's hard to distinguish real params.
     */
    public enum RetType {
        INT, VOID
    }
    // basic information
    private String ident;
    private BasicBlock body;
    private RetType retType;
    private boolean isMain;
    private List<Symbol> params;

    public FuncFrame(FuncDefNode funcDef) {
        this.ident = funcDef.getIdent();
        this.retType = funcDef.getFuncType().equals(TkType.VOIDTK) ? RetType.VOID : RetType.INT;
        this.isMain = "main".equals(this.ident) && RetType.INT.equals(this.retType);
        // fill format params into symtab of body
        this.params = new ArrayList<>();
        this.body = new BasicBlock(funcDef, params);
    }

    // ir part


    // basic function
    public String getIdent() {
        return ident;
    }

    public BasicBlock getBody() {
        return body;
    }

    public RetType getRetType() {
        return retType;
    }

    public boolean isMain() {
        return isMain;
    }

    public List<Symbol> getParams() {
        return params;
    }
}
