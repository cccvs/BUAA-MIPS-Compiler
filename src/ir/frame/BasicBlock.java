package ir.frame;

import ast.func.FuncDefNode;
import ast.func.FuncFParamNode;
import ast.stmt.BlockNode;
import ir.MidCode;
import ir.code.BasicIns;
import ir.operand.Symbol;
import ir.operand.TmpVar;

import java.util.ArrayList;
import java.util.HashMap;
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
        return "label_" + id;
    }

    public BasicBlock prev() {
        return prev;
    }
}
