package mid.code;

import mid.operand.MidVar;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class BasicIns {

    private static int idCnt = 0;

    private boolean dead = false;
    private final int id;

    public BasicIns() {
        this.id = ++idCnt;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MidVar) {
            return Objects.equals(id, ((BasicIns) obj).id);
        }
        return false;
    }
}
