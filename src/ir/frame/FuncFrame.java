package ir.frame;

import ast.func.FuncDefNode;
import ir.operand.Symbol;
import util.TkType;

import java.util.ArrayList;
import java.util.Iterator;
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
    private RetType retType;
    private List<Symbol> params;
    private BasicBlock body;

    public FuncFrame(String ident, TkType tkType) {
        this.ident = ident;
        this.retType = tkType.equals(TkType.VOIDTK) ? RetType.VOID : RetType.INT;
        this.params = null;
        this.body = null;
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

    public Iterator<Symbol> iterParam() {
        return params.iterator();
    }

    public void addParam(Symbol param) {
        params.add(param);
    }

    public void setBody(BasicBlock body) {
        this.body = body;
    }

    public String getLabel() {
        return "f_" + ident;
    }
}
