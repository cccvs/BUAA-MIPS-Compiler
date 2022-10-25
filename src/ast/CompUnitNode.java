package ast;

import ast.decl.DeclNode;
import ast.func.FuncDefNode;

import java.util.ArrayList;

public class CompUnitNode {
    private ArrayList<DeclNode> declNodes;
    private ArrayList<FuncDefNode> funcDefNodes;
    private FuncDefNode mainFuncDefNode;

    public CompUnitNode() {
        this.declNodes = new ArrayList<>();
        this.funcDefNodes = new ArrayList<>();
        this.mainFuncDefNode = null;
    }

    public void addDecl(DeclNode declNode) {
        declNodes.add(declNode);
    }

    public void addFuncDef(FuncDefNode funcDefNode) {
        funcDefNodes.add(funcDefNode);
    }

    public void setMainFuncDef(FuncDefNode mainFuncDefNode) {
        this.mainFuncDefNode = mainFuncDefNode;
    }
}
