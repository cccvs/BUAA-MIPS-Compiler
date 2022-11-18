package back.ins;

import back.Reg;
import back.special.MipsIns;

// pseudo ins, only for print str part, and array part in transOffset
public class La extends MipsIns {
    private final int dst;
    private final String label;
    private final Integer offsetReg;

    public La(int dst, String label) {
        this.dst = dst;
        this.label = label;
        this.offsetReg = null;
    }

    public La(int dst, String label, Integer offsetReg) {
        this.dst = dst;
        this.label = label;
        this.offsetReg = offsetReg;
    }

    public int getDst() {
        return dst;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        if (offsetReg == null) {
            return String.format("la $%s, %s", Reg.name(dst), label);
        } else {
            return String.format("la $%s, %s($%s)", Reg.name(dst), label, Reg.name(offsetReg));
        }
    }
}
