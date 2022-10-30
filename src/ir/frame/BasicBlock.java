package ir.frame;

import ast.func.FuncDefNode;
import ast.func.FuncFParamNode;
import ast.stmt.BlockNode;
import ir.code.BasicIns;
import ir.operand.Symbol;
import ir.operand.TmpVar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BasicBlock implements BasicIns {
    public enum Type {
        FUNC,
        BRANCH,
        LOOP,
        BASIC,   // for loop
    }

    // basic information
    private Type blockType;
    // sym tab information
    protected SymTab symTab;
    private int stackSize = 0;
    // tmp tab information
    private HashMap<Integer, TmpVar> tmpTab = new HashMap<>();    // id to tmpVar
    private int tmpSize = 0;
    // mid ins
    private List<BasicIns> insList = new ArrayList<>();

    public BasicBlock(FuncDefNode funcDef, List<Symbol> params) {
        this.blockType = Type.FUNC;
        // params part
        this.symTab = new SymTab();
        for (FuncFParamNode param : funcDef.getParams()) {
            Symbol symbol = new Symbol(this.symTab, param);
            params.add(symbol);
            symTab.putSym(symbol);
        }
        // translate stmt
        // ...
        // ...
    }


    // ir part
    public void genIns() {

    }

    public int getTmpOffset() {
        int originSize = tmpSize;
        tmpSize += 4;
        return originSize;
    }


    // basic function
    public void putTmp(TmpVar tmpVar) {
        tmpTab.put(tmpVar.getId(), tmpVar);
    }
}
