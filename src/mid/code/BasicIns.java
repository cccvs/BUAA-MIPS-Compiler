package mid.code;

import mid.operand.MidVar;

import java.util.Set;

public interface BasicIns {

    public Set<MidVar> leftSet();

    public Set<MidVar> rightSet();
}
