package front.ast;

import front.ast.decl.DeclNode;
import front.ast.func.FuncDefNode;

import java.util.ArrayList;
import java.util.Iterator;

public class CompUnitNode {
    private ArrayList<DeclNode> declNodes;
    private ArrayList<FuncDefNode> funcDefNodes;
    private FuncDefNode mainFuncDefNode;

    public CompUnitNode() {
        this.declNodes = new ArrayList<>();
        this.funcDefNodes = new ArrayList<>();
        this.mainFuncDefNode = null;
    }

    // basic
    public void addDecl(DeclNode declNode) {
        declNodes.add(declNode);
    }

    public void addFuncDef(FuncDefNode funcDefNode) {
        funcDefNodes.add(funcDefNode);
    }

    public void setMainFuncDef(FuncDefNode mainFuncDefNode) {
        this.mainFuncDefNode = mainFuncDefNode;
    }

    public Iterator<DeclNode> getDeclIter() {
        return declNodes.iterator();
    }

    public Iterator<FuncDefNode> getFuncIter() {
        return funcDefNodes.iterator();
    }

    public FuncDefNode getMainFuncDefNode() {
        return mainFuncDefNode;
    }
}
