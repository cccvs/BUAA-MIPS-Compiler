package mid.code;

import mid.operand.MidVar;

import java.util.HashSet;
import java.util.Set;

public abstract class BasicIns {

    private boolean dead = false;

    public boolean isDead() {
        return dead;
    }

    public void setDead() {
        this.dead = true;
    }

    public Set<MidVar> leftSet() {
        return new HashSet<>();
    }

    public Set<MidVar> rightSet() {
        return new HashSet<>();
    }
}
