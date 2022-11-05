package ir.frame;

import ir.operand.Symbol;

import java.util.HashMap;

public class SymTab {
    // local
    private HashMap<String, Symbol> symTab;
    private SymTab prevTab;       // 对于函数的第一层，prevTab是空

    public SymTab() {
        this.symTab = new HashMap<>();
        this.prevTab = null;
    }

    public SymTab(SymTab prevTab) {
        this.symTab = new HashMap<>();
        this.prevTab = prevTab;
    }

    // ir part
    public Symbol findSym(String symIdent) {
        SymTab tab = this;
        while (tab != null && !tab.symTab.containsKey(symIdent)) {
            tab = tab.prevTab;
        }
        if (tab != null) {
            return tab.symTab.get(symIdent);
        } else {
            return null;
        }
    }

    // basic
    public void putSym(Symbol symbol) {
        symTab.put(symbol.getIdent(), symbol);
    }

    public boolean isGlobal() {
        return prevTab == null;
    }

    public SymTab prev() {
        return prevTab;
    }
}