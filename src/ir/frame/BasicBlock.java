package ir.frame;

import ir.MidCode;
import ir.code.BasicIns;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BasicBlock {
    public enum Type {
        branch_then, branch_else, branch_end,
        loop_begin, loop_body, loop_end,
        and, or,
        func,
        break_follow, continue_follow
    }

    // basic information
    private final Integer id;
    private final Type type;
    private final List<BasicIns> insList = new ArrayList<>();

    public BasicBlock(Type type) {
        this.id = MidCode.genId();
        this.type = type;
    }

    public void append(BasicIns ins) {
        insList.add(ins);
    }

    public String getLabel() {
        return type.name() + "_" + id;
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
