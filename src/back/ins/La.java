package back.ins;

import back.special.MipsIns;

// pseudo ins, only for print str part
public class La extends MipsIns {
    private final int dst;
    private final String label;

    public La(int dst, String label) {
        this.dst = dst;
        this.label = label;
    }

    public int getDst() {
        return dst;
    }

    public String getLabel() {
        return label;
    }
}
