package back.ins;

public class Sw extends MipsIns{
    private final Integer reg;
    private final Integer offset;
    private final Integer base;

    public Sw(Integer reg, Integer offset, Integer base) {
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
}
