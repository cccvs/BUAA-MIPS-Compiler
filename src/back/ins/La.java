package back.ins;

import back.Reg;
import back.special.MipsIns;

// pseudo ins, only for print str part, and array part in transOffset
public class La extends MipsIns {
    private final int dst;
    private final String label;
    private final Integer base;

    public La(int dst, String label) {
        this.dst = dst;
        this.label = label;
        this.base = null;
    }

    public La(int dst, String label, Integer base) {
        this.dst = dst;
        this.label = label;
        this.base = base;
    }

    public int getDst() {
        return dst;
    }

    public String getLabel() {
        return label;
    }

    public Integer getBase() {
        return base;
    }

    @Override
    public String toString() {
        if (base == null) {
            return String.format("la $%s, %s", Reg.name(dst), label);
        } else {
            return String.format("la $%s, %s($%s)", Reg.name(dst), label, Reg.name(base));
        }
    }
}
