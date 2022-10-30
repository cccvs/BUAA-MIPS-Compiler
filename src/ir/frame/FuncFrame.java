package ir.frame;

import ast.func.FuncDefNode;
import ast.func.FuncFParamNode;
import ir.operand.Symbol;
import util.TkType;

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
        this.body = new BasicBlock(BasicBlock.Type.FUNC);
        this.retType = funcDef.getFuncType().equals(TkType.VOIDTK) ? RetType.VOID : RetType.INT;
        this.isMain = "main".equals(this.ident) && RetType.INT.equals(this.retType);
        for (FuncFParamNode param : funcDef.getParams()) {
            Symbol symbol = new Symbol(this, param);
            this.params.add(symbol);
            this.body.putSym(symbol);
        }
        // fill remain information
        this.body.fillInfo(funcDef.getBlock());
    }

    // ir part
    public int getStackOffset(int newSize) {
        return this.body.getStackOffset(newSize);
    }

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
}
