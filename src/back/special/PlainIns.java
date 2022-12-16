package back.special;

public class PlainIns extends MipsIns{
    private final String ins;

    public PlainIns(String ins) {
        this.ins = ins;
    }

    @Override
    public String toString() {
        return ins;
    }
}
