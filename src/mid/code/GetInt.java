package mid.code;

import mid.operand.MidVar;

import java.util.HashSet;
import java.util.Set;

public class GetInt extends BasicIns {
    private final MidVar var;

    public GetInt(MidVar var) {
        super();
        this.var = var;
    }

    public MidVar getVar() {
        return var;
    }

    @Override
    public String toString() {
        return "\t" + "GETINT " + var;
    }

    @Override
    public Set<MidVar> leftSet() {
        Set<MidVar> leftSet = new HashSet<>();
        if (var != null) {
            leftSet.add(var);
        }
        return leftSet;
    }

    @Override
    public Set<MidVar> rightSet() {
        return new HashSet<>();
    }
}
