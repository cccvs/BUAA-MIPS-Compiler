package back.ins;

import back.special.MipsIns;

public class Mult extends MipsIns {
    private final int src1;
    private final int src2;

    public Mult(int src1, int src2) {
        this.src1 = src1;
        this.src2 = src2;
    }

    public int getSrc1() {
        return src1;
    }

    public int getSrc2() {
        return src2;
    }
}
