package back.ins;

import back.special.MipsIns;

public class Jr extends MipsIns {
    private final int src;

    public Jr(int src) {
        this.src = src;
    }

    public int getSrc() {
        return src;
    }
}
