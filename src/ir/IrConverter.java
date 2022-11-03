package ir;

import ast.CompUnitNode;
import ast.decl.DeclNode;
import ast.decl.DefNode;
import ast.func.FuncDefNode;
import ast.stmt.BlockNode;
import ir.frame.FuncFrame;
import ir.frame.SymTab;
import ir.operand.Symbol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IrConverter {
    private static List<Symbol> globalSyms = new ArrayList<>();
    private static SymTab curTab = new SymTab();   // 初始化为全局符号表
    private static MidCode midCode = new MidCode();

    public IrConverter(CompUnitNode compUnitNode) {
        convCompUnit(compUnitNode);
        // testGlobal();
    }

    public void convCompUnit(CompUnitNode compUnit) {
        Iterator<DeclNode> globalDecls = compUnit.getDeclIter();
        while (globalDecls.hasNext()) {
            DeclNode decl = globalDecls.next();
            convDecl(decl);
        }
        Iterator<FuncDefNode> funcDefs = compUnit.getFuncIter();
        while (funcDefs.hasNext()) {
            FuncDefNode funcDef = funcDefs.next();

        }
    }

    private void convDecl(DeclNode decl) {
        Iterator<DefNode> globalDefs = decl.defIter();
        while (globalDefs.hasNext()) {
            DefNode def = globalDefs.next();
            convDef(def);
        }
    }

    private void convDef(DefNode def) {
        Symbol symbol = new Symbol(def);
        curTab.putSym(symbol);
        if (curTab.isGlobal()) {
            globalSyms.add(symbol);
        }
    }

    private void convFunc(FuncDefNode funcDef) {
        FuncFrame funcFrame = new FuncFrame(funcDef.getIdent(), funcDef.getFuncType());

        MidCode.putFunc(funcFrame);
    }

    private void convBlock(BlockNode block) {
        curTab = new SymTab(curTab);

        curTab = curTab.prev();
    }

    // functional
    public static Symbol getGlobalSym(String ident) {
        assert curTab.isGlobal();
        return curTab.findSym(ident);
    }

    // test
    private void testGlobal() {
        for (Symbol symbol : globalSyms) {
            System.out.println(symbol.getIdent() + "[0x" + Integer.toHexString(symbol.getStackOffset()) + "]");
        }
    }
}
