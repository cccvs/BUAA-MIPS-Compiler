package mid.code;

import mid.operand.MidVar;

import java.util.HashSet;
import java.util.Set;

public class PrintStr extends BasicIns{
    // Originate from printf
    private final String label;

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

    @Override
    public Set<MidVar> leftSet() {
        return new HashSet<>();
    }

    @Override
    public Set<MidVar> rightSet() {
        return new HashSet<>();
    }
}
