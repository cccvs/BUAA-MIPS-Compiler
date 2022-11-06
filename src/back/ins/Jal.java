package back.ins;

import back.Reg;
import back.special.MipsIns;

public class Jal extends MipsIns {
    private final String label;

    public Jal(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return String.format("jal %s", label);
    }
}
