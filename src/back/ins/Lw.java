package back.ins;

import back.Reg;
import back.special.MipsIns;

public class Lw extends MipsIns {
    private final Integer reg;
    private final Integer offset;
    private final Integer base;

    public Lw(Integer reg, Integer offset, Integer base) {
        this.reg = reg;
        this.offset = offset;
        this.base = base;
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
        return String.format("lw $%s, %d($%s)", Reg.name(reg), offset, Reg.name(base));
    }
}
