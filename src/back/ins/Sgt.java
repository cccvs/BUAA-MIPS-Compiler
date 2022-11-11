package back.ins;

import back.Reg;
import back.special.MipsIns;

public class Sgt extends MipsIns {
    private final int dst;
    private final int src1;
    private final int src2;

    public Sgt(int dst, int src1, int src2) {
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
    }

    public int getDst() {
        return dst;
    }

    public int getSrc1() {
        return src1;
    }

    public int getSrc2() {
        return src2;
    }

    @Override
    public String toString() {
        return String.format("sgt $%s, $%s, $%s", Reg.name(dst), Reg.name(src1), Reg.name(src2));
    }
}
