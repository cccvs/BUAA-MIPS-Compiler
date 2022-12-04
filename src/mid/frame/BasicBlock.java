package mid.frame;

import mid.MidCode;
import mid.code.BasicIns;
import mid.code.Return;
import mid.operand.MidVar;

import java.util.*;

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

    // optimize
    private final List<BasicBlock> preBlocks = new ArrayList<>();
    private final List<BasicBlock> subBlocks = new ArrayList<>();
    private final Set<MidVar> liveGen = new HashSet<>();
    private final Set<MidVar> liveKill = new HashSet<>();
    private final Set<MidVar> liveIn = new HashSet<>();
    private final Set<MidVar> liveOut = new HashSet<>();

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

    public BasicIns getLastIns() {
        return insList.isEmpty() ? null : insList.get(insList.size() - 1);
    }

    public void linkNext(BasicBlock next) {
        subBlocks.add(next);
        next.preBlocks.add(this);
    }
    // optimize

    public void calGenKill() {
        liveGen.clear();
        liveKill.clear();
        for (BasicIns basicIns : insList) {

        }
    }

    public void clearReturnFollows() {
        for (int i = 0; i < insList.size(); i++) {
            if (insList.get(i) instanceof Return) {
                if (insList.size() > i + 1) {
                    insList.subList(i + 1, insList.size()).clear();
                    return;
                }
            }
        }
    }

    @Override
    public String toString() {
        return getLabel() + ":\n" +
                insList.stream().map(BasicIns::toString).reduce((x, y) -> x + "\n" + y).orElse("") + "\n";
    }
}
