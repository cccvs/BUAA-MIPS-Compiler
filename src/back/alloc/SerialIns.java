package back.alloc;

import mid.code.BasicIns;
import mid.operand.MidVar;

import java.util.Set;

public class SerialIns {
    private final int pos;
    private final BasicIns basicIns;

    public SerialIns(int pos, BasicIns basicIns) {
        this.pos = pos;
        this.basicIns = basicIns;
    }

    public int getPos() {
        return pos;
    }

    public BasicIns getIns() {
        return basicIns;
    }

    public Set<MidVar> leftSet() {
        return basicIns.leftSet();
    }

    public Set<MidVar> rightSet() {
        return basicIns.rightSet();
    }
}
