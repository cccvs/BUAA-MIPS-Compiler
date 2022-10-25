package ast.stmt;

import java.util.ArrayList;

public class BlockNode implements StmtNode {
    private ArrayList<BlockItemNode> blockItemNodes;

    public BlockNode() {
        this.blockItemNodes = new ArrayList<>();
    }

    public void addItem(BlockItemNode item) {
        blockItemNodes.add(item);
    }
}
