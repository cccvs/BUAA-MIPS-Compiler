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
    private final String ident;
    private final RetType retType;
    private final List<Symbol> params;
    private final List<BasicBlock> bodyBlocks;
    private int stackSize = 0;

    public FuncFrame(String ident, TkType tkType) {
        this.ident = ident;
        this.retType = tkType.equals(TkType.VOIDTK) ? RetType.VOID : RetType.INT;
        this.params = new ArrayList<>();
        this.bodyBlocks = new ArrayList<>();
    }

    public int addStackSize(int size) {
        stackSize += size;
        return stackSize;
    }

    // basic function
    public String getIdent() {
        return ident;
    }

    public Iterator<BasicBlock> iterBody() {
        return bodyBlocks.iterator();
    }

    public RetType getRetType() {
        return retType;
    }

    public Iterator<Symbol> iterFormatParam() {
        return params.iterator();
    }

    public void addParam(Symbol param) {
        params.add(param);
    }

    public void appendBody(BasicBlock body) {
        this.bodyBlocks.add(body);
    }

    public String getLabel() {
        return "f_" + ident;
    }

    @Override
    public String toString() {
        // System.out.println(getLabel());
        return "# Function " + ident + "[stack size: 0x" +
                Integer.toHexString(stackSize) + "]\n" +
                getLabel() + ":\n" +
                bodyBlocks.stream().map(BasicBlock::toString).reduce((x, y) -> x + y).orElse("");
    }
}
