package back.ins;

public class Li extends MipsIns{
    private final int dst;
    private final int imm;

    public Li(int dst, int imm) {
        this.dst = dst;
        this.imm = imm;
    }

    public int getDst() {
        return dst;
    }

    public int getImm() {
        return imm;
    }
}
