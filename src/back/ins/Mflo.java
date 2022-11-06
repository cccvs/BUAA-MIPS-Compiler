package back.ins;

import back.special.MipsIns;

public class Mflo extends MipsIns {
    private final int dst;

    public Mflo(int dst) {
        this.dst = dst;
    }

    public int getDst() {
        return dst;
    }
}
