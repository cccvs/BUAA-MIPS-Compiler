package mid.code;

import mid.operand.Symbol;

public class GetInt implements BasicIns{
    private Symbol var;

    public GetInt(Symbol var) {
        this.var = var;
    }

    public Symbol getVar() {
        return var;
    }

    @Override
    public String toString() {
        return "\t" + "GETINT " + var;
    }
}
