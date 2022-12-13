package back.ins;

import back.Reg;
import back.special.MipsIns;

public class Slei extends MipsIns {
    private final int dst;
    private final int src1;
    private final int imm;

    public Slei(int dst, int src1, int imm) {
        this.dst = dst;
        this.src1 = src1;
        this.imm = imm;
    }

    @Override
    public String toString() {
        return String.format("sle $%s, $%s, $%s", Reg.name(dst), Reg.name(src1), Reg.name(imm));
    }
}
