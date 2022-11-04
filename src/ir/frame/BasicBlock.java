package ir.frame;

import ast.func.FuncDefNode;
import ast.func.FuncFParamNode;
import ast.stmt.BlockNode;
import ir.MidCode;
import ir.code.BasicIns;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock implements BasicIns {
    public enum Type {
        FUNC,
        BRANCH,
        LOOP,
        BASIC,   // for loop
    }

    // basic information
    private Integer id;
    private BasicBlock prev;
    private final List<BasicIns> insList = new ArrayList<>();

    public BasicBlock() {
        this.id = MidCode.genId();
        this.prev = null;
    }

    public BasicBlock(BasicBlock prev) {
        this.id = MidCode.genId();
        this.prev = prev;
    }

    public void append(BasicIns ins) {
        insList.add(ins);
    }

    public String getLabel() {
        return "label" + id;
    }

    public BasicBlock prev() {
        return prev;
    }

    @Override
    public String toString() {
        return getLabel() + ":\n" +
                insList.stream().map(BasicIns::toString).reduce((x, y) -> x + "\n" + y).orElse("") + "\n";
    }
}
