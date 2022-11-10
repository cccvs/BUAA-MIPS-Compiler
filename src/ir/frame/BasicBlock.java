package ir.frame;

import ir.MidCode;
import ir.code.BasicIns;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BasicBlock {
    public enum Type {
        FUNC,
        BRANCH,
        LOOP,
        BASIC,   // for loop
    }

    // basic information
    private final Integer id;
    private final List<BasicIns> insList = new ArrayList<>();

    public BasicBlock() {
        this.id = MidCode.genId();
    }

    public void append(BasicIns ins) {
        insList.add(ins);
    }

    public String getLabel() {
        return "label" + id;
    }

    public Iterator<BasicIns> iterIns() {
        return insList.iterator();
    }

    @Override
    public String toString() {
        return getLabel() + ":\n" +
                insList.stream().map(BasicIns::toString).reduce((x, y) -> x + "\n" + y).orElse("") + "\n";
    }
}
