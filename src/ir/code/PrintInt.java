package ir.code;

import ir.operand.Operand;

public class PrintInt implements BasicIns{
    // Originate from printf
    private Operand src;

    public PrintInt(Operand src) {
        this.src = src;
    }
}
