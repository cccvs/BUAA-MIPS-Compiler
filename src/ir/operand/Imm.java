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


}
