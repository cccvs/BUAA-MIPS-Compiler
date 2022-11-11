package front.ast.stmt;
import java.util.ArrayList;
import java.util.Iterator;

public class BlockNode implements StmtNode {
    private ArrayList<BlockItemNode> blockItemNodes;

    public BlockNode() {
        this.blockItemNodes = new ArrayList<>();
    }

    // basic method
    public void addItem(BlockItemNode item) {
        blockItemNodes.add(item);
    }

    public Iterator<BlockItemNode> iterBlockItem() {
        return blockItemNodes.iterator();
    }
}
