package ir.operand;

public class Imm implements Operand{
    public int val;

    public Imm(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    @Override
    public Integer getOffset() {
        assert false;
        System.exit(7);
        return null;
    }

    @Override
    public String toString() {
        return val + "[v]";
    }

    @Override
    public RefType getRefType() {
        return RefType.VALUE;
    }

    @Override
    public Integer getId() {
        return null;
    }
}
