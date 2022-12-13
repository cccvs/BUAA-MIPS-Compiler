package back.ins;

import back.special.MipsIns;
import back.Reg;


public class Muli extends MipsIns {
    private final int dst;
    private final int src1;
    private final int imm;

    public Muli(int dst, int src1, int imm) {
        this.dst = dst;
        this.src1 = src1;
        this.imm = imm;
    }

    public int getSrc1() {
        return src1;
    }


    @Override
    public String toString() {
        return String.format("mul $%s, $%s, %d", Reg.name(dst), Reg.name(src1), imm);
    }
}
