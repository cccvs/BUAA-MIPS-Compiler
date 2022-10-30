package ir;

public class BasicBlock {
    public enum Type {
        FUNC,
        BRANCH,
        LOOP,
        BASIC,   // for loop
    }

    // counter related to block
    private static int indexCount = 0;
    private static int blockCount = 0;

    // type of block
    private Type blockType;
    private int blockId;

    public BasicBlock(Type type) {
        this.blockId = ++blockCount;
        this.blockType = type;
    }

    public String genName() {
        return "t" + (++indexCount);
    }
}
