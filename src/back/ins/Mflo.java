package back.ins;

public class Mflo extends MipsIns{
    private final int dst;

    public Mflo(int dst) {
        this.dst = dst;
    }

    public int getDst() {
        return dst;
    }
}
