package ir.code;

import ir.operand.Symbol;

public class GetInt implements BasicIns{
    private Symbol var;

    public GetInt(Symbol var) {
        this.var = var;
    }

    @Override
    public String toString() {
        return "\t" + "GETINT " + var;
    }
}
