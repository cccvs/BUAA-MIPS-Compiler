package back.ins;

import back.Reg;
import back.special.MipsIns;

public class Bnez extends MipsIns {
    private final Integer src;
    private final String label;

    public Bnez(Integer src, String label) {
        this.src = src;
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("bnez $%s, %s", Reg.name(src), label);
    }
}
