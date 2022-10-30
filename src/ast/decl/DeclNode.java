package ast.decl;

import ast.stmt.BlockItemNode;

import java.util.ArrayList;

public class DeclNode implements BlockItemNode {
    private final boolean isConst;
    private final ArrayList<DefNode> defNodes;

    public DeclNode(boolean isConst) {
        this.isConst = isConst;
        this.defNodes = new ArrayList<>();
    }

    // syntax part
    public void addDefs(DefNode defNode) {
        defNodes.add(defNode);
    }

    // ir part
    public void fillGlobalSymTab() {
        defNodes.forEach(DefNode::fillGlobalSymTab);
    }
}
