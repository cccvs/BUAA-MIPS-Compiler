package ir.code;

import ir.operand.Symbol;

public class Input implements BasicIns{
    private Symbol var;

    public Input(Symbol var) {
        this.var = var;
    }
}
