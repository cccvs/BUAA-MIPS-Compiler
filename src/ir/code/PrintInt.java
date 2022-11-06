package ir.code;

import ir.operand.Operand;

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
}
