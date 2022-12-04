package mid.code;

import mid.operand.MidVar;
import mid.operand.Operand;

import java.util.HashSet;
import java.util.Set;

public class PrintInt implements BasicIns{
    // Originate from printf
    private Operand src;

    public PrintInt(Operand src) {
        this.src = src;
    }

    public Operand getSrc() {
        return src;
    }

    @Override
    public String toString() {
        return "\tPRINT_INT " + src;
    }

    @Override
    public Set<MidVar> leftSet() {
        return new HashSet<>();
    }

    @Override
    public Set<MidVar> rightSet() {
        Set<MidVar> rightSet = new HashSet<>();
        if (src instanceof MidVar) {
            rightSet.add((MidVar) src);
        }
        return rightSet;
    }
}
