package back.alloc;

import mid.operand.MidVar;

import java.util.LinkedList;
import java.util.List;

public class LiveInterval {
    private final List<IntPair> pairs;
    private final MidVar midVar;

    public LiveInterval(MidVar midVar) {
        this.pairs = new LinkedList<>();
        this.midVar = midVar;
    }

    // 从大到小的顺序添加区间，每次加在区间开头
    public void addPair(int lower, int upper) {
        if (pairs.isEmpty() || upper + 1 < pairs.get(0).getLower()) {
            pairs.add(0, new IntPair(lower, upper));
        } else {
            int newUpper = pairs.get(0).getUpper();
            pairs.remove(0);
            pairs.add(0, new IntPair(lower, newUpper));
        }
    }
}
