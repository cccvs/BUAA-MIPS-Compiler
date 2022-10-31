package ir.code;

import ir.operand.Operand;
import ir.operand.Symbol;

public class MemOp implements BasicIns{
    // Originate from Assign, Def/Decl, Exp(LVal)
    public enum Type {
        LOAD,
        STORE
    }

    private Type op;
    private Symbol pointer;
    private Operand operand;

    public MemOp(Type op, Symbol pointer, Operand operand) {
        this.op = op;
        this.pointer = pointer;
        this.operand = operand;
    }
}
