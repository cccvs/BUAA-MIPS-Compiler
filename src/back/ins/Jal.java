package back.ins;

public class Jal extends MipsIns{
    private final String label;

    public Jal(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
