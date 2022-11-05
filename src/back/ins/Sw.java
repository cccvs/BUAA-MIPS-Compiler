package back.ins;

public class Sw extends MipsIns{
    private final Integer offset;
    private final Integer reg;

    public Sw(Integer offset, Integer reg) {
        this.offset = offset;
        this.reg = reg;
    }

    public Integer getOffset() {
        return offset;
    }

    public Integer getReg() {
        return reg;
    }
}
