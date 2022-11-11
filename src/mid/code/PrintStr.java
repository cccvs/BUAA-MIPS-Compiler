package mid.code;

public class PrintStr implements BasicIns{
    // Originate from printf
    private String label;

    public PrintStr(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "\tPRINT_STR " + label;
    }
}
