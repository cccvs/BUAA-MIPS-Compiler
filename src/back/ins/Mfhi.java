package back.ins;

import back.special.MipsIns;

public class Mfhi extends MipsIns {
    private final int dst;

    public Mfhi(int dst) {
        this.dst = dst;
    }

    public int getDst() {
        return dst;
    }
}
