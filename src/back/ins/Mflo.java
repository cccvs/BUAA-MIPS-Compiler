package back.ins;

import back.Reg;
import back.special.MipsIns;

public class Mflo extends MipsIns {
    private final int dst;

    public Mflo(int dst) {
        this.dst = dst;
    }

    public int getDst() {
        return dst;
    }

    @Override
    public String toString() {
        return String.format("mflo $%s", Reg.name(dst));
    }
}
