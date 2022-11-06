package back.ins;

import back.Reg;
import back.special.MipsIns;

public class Addi extends MipsIns {
    private final int dst;
    private final int src1;
    private final int imm;

    public Addi(int dst, int src1, int imm) {
        this.dst = dst;
        this.src1 = src1;
        this.imm = imm;
    }

    public int getDst() {
        return dst;
    }

    public int getSrc1() {
        return src1;
    }

    public int getImm() {
        return imm;
    }

    @Override
    public String toString() {
        return String.format("addi $%s, $%s, %d", Reg.name(dst), Reg.name(src1), imm);
    }
}
