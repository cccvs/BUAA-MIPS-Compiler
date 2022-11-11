package front.ast.decl;

import front.ast.stmt.BlockItemNode;

import java.util.ArrayList;
import java.util.Iterator;

public class DeclNode implements BlockItemNode {
    private final boolean isConst;
    private final ArrayList<DefNode> defNodes;

    public DeclNode(boolean isConst) {
        this.isConst = isConst;
        this.defNodes = new ArrayList<>();
    }

    public void addDefs(DefNode defNode) {
        defNodes.add(defNode);
    }

    public Iterator<DefNode> defIter() {
        return defNodes.iterator();
    }

}
