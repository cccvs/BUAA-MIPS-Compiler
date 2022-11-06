package ir.operand;

public class Imm implements Operand{
    private final int val;

    public Imm(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    @Override
    public String toString() {
        return val + "[v]";
    }

    @Override
    public RefType getRefType() {
        return RefType.VALUE;
    }
}
