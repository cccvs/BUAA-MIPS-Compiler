package back.ins;

import back.Reg;
import back.special.MipsIns;

public class Sgti extends MipsIns {
    private final int dst;
    private final int src1;
    private final int imm;

    public Sgti(int dst, int src1, int imm) {
        this.dst = dst;
        this.src1 = src1;
        this.imm = imm;
    }

    @Override
    public String toString() {
        return String.format("sgt $%s, $%s, %d", Reg.name(dst), Reg.name(src1), imm);
    }
}