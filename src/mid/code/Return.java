package mid.code;

import mid.operand.MidVar;
import mid.operand.Operand;

import java.util.HashSet;
import java.util.Set;

public class Return extends BasicIns{
    // Originate from Return
    private Operand retVal;   // null if void

    public Return(Operand retVal) {
        super();
        this.retVal = retVal;
    }

    public Return() {
        this.retVal = null;
    }

    public Operand getRetVal() {
        return retVal;
    }

    public void setRetVal(Operand retVal) {
        this.retVal = retVal;
    }

    @Override
    public String toString() {
        return "\tRETURN " + retVal;
    }

    @Override
    public Set<MidVar> leftSet() {
        return new HashSet<>();
    }

    @Override
    public Set<MidVar> rightSet() {
        Set<MidVar> rightSet = new HashSet<>();
        if (retVal instanceof MidVar) {
            rightSet.add((MidVar) retVal);
        }
        return rightSet;
    }
}
