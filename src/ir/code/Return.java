package ir.code;

import ir.operand.Operand;

public class Return implements BasicIns{
    // Originate from Return
    private final Operand retVal;   // null if void

    public Return(Operand retVal) {
        this.retVal = retVal;
    }

    public Return() {
        this.retVal = null;
    }

    public Operand getRetVal() {
        return retVal;
    }

    @Override
    public String toString() {
        return "\tRETURN " + retVal;
    }
}
