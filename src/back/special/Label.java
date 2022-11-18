package back.special;

public class Label extends MipsIns{
    private final String label;

    public Label(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "\n\t" + label + ":";
    }
}
