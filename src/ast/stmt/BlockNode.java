package ast.stmt;

import ir.frame.BasicBlock;

import java.util.ArrayList;
import java.util.Iterator;

public class BlockNode implements StmtNode {
    private ArrayList<BlockItemNode> blockItemNodes;

    public BlockNode() {
        this.blockItemNodes = new ArrayList<>();
    }

    // ir part
    public void toIr(BasicBlock basicBlock) {
        // TODO[6]: can be optimized?
        BasicBlock son = new BasicBlock(basicBlock);
        basicBlock.append(son);
    }

    // basic method
    public void addItem(BlockItemNode item) {
        blockItemNodes.add(item);
    }

    public Iterator<BlockItemNode> iterBlockItem() {
        return blockItemNodes.iterator();
    }
}
