package mid.code;

import mid.frame.MidLabel;
import mid.operand.MidVar;
import mid.operand.Operand;

import java.util.HashSet;
import java.util.Set;

public class Branch implements BasicIns{
    // only list basic mips instruction
    public enum Type {
        BNEZ
        //, BEZ,
        //BEQ, BNE
    }

    private final Type type;
    private final Operand cond;
    private final MidLabel labelTrue;
    private final MidLabel labelFalse;

    public Branch(Type type, Operand cond, MidLabel labelTrue, MidLabel labelFalse) {
        this.type = type;
        this.cond = cond;
        this.labelTrue = labelTrue;
        this.labelFalse = labelFalse;
    }

    public Type getType() {
        return type;
    }

    public MidLabel getLabelTrue() {
        return labelTrue;
    }

    public MidLabel getLabelFalse() {
        return labelFalse;
    }

    public Operand getCond() {
        return cond;
    }

    @Override
    public String toString() {
        return "\t" + type.name() + " " + cond.toString() + " " +
                labelTrue.getLabel() + ", " + labelFalse.getLabel();
    }

    @Override
    public Set<MidVar> leftSet() {
        return new HashSet<>();
    }

    @Override
    public Set<MidVar> rightSet() {
        Set<MidVar> rightSet = new HashSet<>();
        if (cond instanceof MidVar) {
            rightSet.add((MidVar) cond);
        }
        return rightSet;
    }
}
