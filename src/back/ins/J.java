package back.ins;

import back.special.MipsIns;

public class J extends MipsIns {
    private final String label;

    public J(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return String.format("j %s", label);
    }
}
