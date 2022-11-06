package back.ins;

import back.Reg;
import back.special.MipsIns;

public class Sw extends MipsIns {
    private final Integer reg;
    private final Integer offset;
    private final Integer base;
    private final String label;

    public Sw(Integer reg, Integer offset, Integer base) {
        this.reg = reg;
        this.offset = offset;
        this.base = base;
        this.label = null;
    }

    public Sw(Integer reg, String label) {
        this.reg = reg;
        this.offset = null;
        this.base = null;
        this.label = label;
    }
    public Integer getReg() {
        return reg;
    }

    public Integer getOffset() {
        return offset;
    }

    public Integer getBase() {
        return base;
    }

    @Override
    public String toString() {
        if (label == null) {
            assert reg != null && offset != null && base != null;
            return String.format("sw $%s, %d($%s)", Reg.name(reg), offset, Reg.name(base));
        } else {
            return String.format("sw $%s, %s", Reg.name(reg), label);
        }
    }
}
