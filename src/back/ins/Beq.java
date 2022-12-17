package back.ins;

import back.Reg;
import back.special.MipsIns;

public class Beq extends MipsIns {
    private final Integer src1;
    private final Integer src2;
    private final String label;

    public Beq(Integer src1, Integer src2, String label) {
        this.src1 = src1;
        this.src2 = src2;
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("beq $%s, $%s, %s", Reg.name(src1), Reg.name(src2), label);
    }
}
