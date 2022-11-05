package back.ins;

// pseudo ins, only for print str part
public class La extends MipsIns{
    private final String label;

    public La(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
