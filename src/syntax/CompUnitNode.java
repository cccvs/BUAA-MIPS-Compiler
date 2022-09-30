package syntax;

import java.util.ArrayList;

public class CompUnitNode {
    private ArrayList<ConstDeclNode> constDecls;
    private ArrayList<VarDeclNode> varDecls;
    private ArrayList<FuncDefNode> funcDefs;
    private FuncDefNode mainFuncDef;

    public CompUnitNode() {
        constDecls = new ArrayList<>();
        varDecls = new ArrayList<>();
        funcDefs = new ArrayList<>();
        mainFuncDef = null;
    }

    public void addConstDecl(ConstDeclNode constDeclNode) {
        constDecls.add(constDeclNode);
    }

    public void addVarDecl(VarDeclNode varDeclNode) {
        varDecls.add(varDeclNode);
    }

    public void addFuncDef(FuncDefNode funcDefNode) {
        funcDefs.add(funcDefNode);
    }

    public void setMainFuncDef(FuncDefNode mainFuncDef) {
        this.mainFuncDef = mainFuncDef;
    }
}
