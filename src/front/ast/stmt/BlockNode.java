package front.ast.stmt;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockNode implements StmtNode {
    private final List<BlockItemNode> blockItemNodes;
    private Integer endLine = null; // 可删除所有end line为空的block

    public BlockNode() {
        this.blockItemNodes = new ArrayList<>();
    }

    // basic method
    public Integer getEndLine() {
        return endLine;
    }

    public void setEndLine(Integer endLine) {
        this.endLine = endLine;
    }

    public void addItem(BlockItemNode item) {
        blockItemNodes.add(item);
    }

    public Iterator<BlockItemNode> iterBlockItem() {
        return blockItemNodes.iterator();
    }

    public BlockItemNode getLastItem() {
        return blockItemNodes.isEmpty() ? null : blockItemNodes.get(blockItemNodes.size() - 1);
    }
}
