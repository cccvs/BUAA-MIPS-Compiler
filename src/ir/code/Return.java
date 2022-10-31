package ir.code;

import ir.operand.Operand;

public class Return implements BasicIns{
    // Originate from Return
    private Operand retVal;

    public Return(Operand retVal) {
        this.retVal = retVal;
    }

    public Return() {
        this.retVal = null;
    }
}
