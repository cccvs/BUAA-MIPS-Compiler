package mid.code;

import mid.operand.MidVar;

public class GetInt implements BasicIns{
    private final MidVar var;

    public GetInt(MidVar var) {
        this.var = var;
    }

    public MidVar getVar() {
        return var;
    }

    @Override
    public String toString() {
        return "\t" + "GETINT " + var;
    }
}
