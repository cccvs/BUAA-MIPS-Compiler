package mid.code;

import mid.frame.MidLabel;
import mid.operand.MidVar;

import java.util.HashSet;
import java.util.Set;

public class Jump extends BasicIns{
    private final MidLabel midLabel;

    public Jump(MidLabel midLabel) {
        super();
        this.midLabel = midLabel;
    }

    public MidLabel getTargetLabel() {
        return midLabel;
    }

    @Override
    public String toString() {
        return "\tJUMP " + midLabel.getLabel();
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
