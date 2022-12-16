package mid.frame;

import mid.MidCode;
import mid.code.BasicIns;
import mid.operand.MidVar;

import java.util.HashSet;
import java.util.Set;

public class MidLabel extends BasicIns {

    public enum Type {
        branch_then, branch_else, branch_end,
        loop_begin, loop_body, loop_end,
        and, or,
        func,
        break_follow, continue_follow
    }

    // basic information
    private final Integer id;
    private final MidLabel.Type type;

    public MidLabel(Type type) {
        super();
        this.id = MidCode.genId();
        this.type = type;
    }

    public String getLabel() {
        return type.name() + "_" + id;
    }

    @Override
    public String toString() {
        return getLabel() + ":\n";
    }

    @Override
    public Set<MidVar> leftSet() {
        return new HashSet<>();
    }

    @Override
    public Set<MidVar> rightSet() {
        return new HashSet<>();
    }
}
